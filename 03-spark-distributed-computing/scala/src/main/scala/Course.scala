package course

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Course {
  val orderColumns = Seq("order_id", "customer_id", "amount", "event_time")
  val customerColumns = Seq("customer_id", "region")

  def run(name: String, args: Array[String])(job: (SparkSession, String) => Unit): Unit = {
    require(args.length <= 1, "Usage: <class> [data-directory]")
    val spark = SparkSession.builder().appName(name)
      .config("spark.sql.session.timeZone", "UTC")
      .config("spark.sql.ansi.enabled", "true")
      .config("spark.sql.legacy.timeParserPolicy", "CORRECTED")
      .config("spark.sql.shuffle.partitions", "4")
      .config("spark.sql.adaptive.enabled", "true")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    try job(spark, args.headOption.getOrElse("data"))
    finally spark.stop()
  }

  def readStrings(spark: SparkSession, path: String, columns: Seq[String]): DataFrame = {
    val schema = StructType(columns.map(name => StructField(name, StringType, true)))
    spark.read.schema(schema).option("header", "true")
      .option("enforceSchema", "false").option("mode", "FAILFAST").csv(path)
  }

  def requireNoRows(frame: DataFrame, message: String): Unit =
    require(frame.limit(1).count() == 0, message)

  def requireUnique(frame: DataFrame, key: String): Unit =
    requireNoRows(frame.groupBy(key).count().filter("count > 1"), s"Duplicate $key")

  def cleanOrders(raw: DataFrame): DataFrame = {
    val parsed = raw
      .withColumn("amount_value", expr("try_cast(amount as decimal(12,2))"))
      .withColumn("event_ts", expr("try_to_timestamp(event_time, 'yyyy-MM-dd HH:mm:ss')"))
    val valid = col("order_id").rlike("^o[0-9]+$") &&
      col("customer_id").rlike("^c[0-9]+$") &&
      col("amount").rlike("^[0-9]+(\\.[0-9]{1,2})?$") &&
      (col("amount_value") > 0) && col("event_ts").isNotNull &&
      (date_format(col("event_ts"), "yyyy-MM-dd HH:mm:ss") === col("event_time"))
    requireNoRows(parsed.filter(!coalesce(valid, lit(false))),
      "Invalid order: required IDs, positive decimal(12,2) amount with <=2 " +
        "fractional digits, and UTC yyyy-MM-dd HH:mm:ss timestamp are required")
    val result = parsed.select(col("order_id"), col("customer_id"),
      col("amount_value").as("amount"), col("event_ts"))
    requireUnique(result, "order_id")
    result
  }

  def cleanCustomers(raw: DataFrame): DataFrame = {
    val valid = col("customer_id").rlike("^c[0-9]+$") && col("region").rlike("^[A-Z]{2}$")
    requireNoRows(raw.filter(!coalesce(valid, lit(false))),
      "Invalid customer: customer_id and two-letter uppercase region required")
    requireUnique(raw, "customer_id")
    raw
  }

  def loadSales(spark: SparkSession, root: String): (DataFrame, DataFrame) = {
    val orders = cleanOrders(readStrings(spark, s"$root/orders.csv", orderColumns))
    val customers = cleanCustomers(readStrings(spark, s"$root/customers.csv", customerColumns))
    requireNoRows(orders.join(customers, Seq("customer_id"), "left_anti"),
      "Unknown customer_id: inner join would lose orders")
    (orders, customers)
  }

  def wordCounts(lines: DataFrame): DataFrame =
    lines.select(explode(split(lower(col("value")), "[^a-z]+")).as("word"))
      .filter(col("word") =!= "").groupBy("word").count()
      .orderBy(col("count").desc, col("word"))

  def latestCustomerSales(orders: DataFrame, customers: DataFrame): DataFrame = {
    val byCustomer = Window.partitionBy("customer_id")
    val latest = byCustomer.orderBy(col("event_ts").desc, col("order_id").desc)
    orders.join(broadcast(customers), Seq("customer_id"), "inner")
      .withColumn("lifetime_total", sum("amount").over(byCustomer))
      .withColumn("position", row_number().over(latest)).filter("position = 1")
      .select("customer_id", "region", "order_id", "amount", "lifetime_total")
      .orderBy("customer_id")
  }

  def regionTotals(orders: DataFrame, customers: DataFrame): DataFrame =
    orders.select("customer_id", "amount", "event_ts")
      .filter(col("event_ts") >= lit("2025-01-01").cast("timestamp"))
      .join(broadcast(customers.select("customer_id", "region")), Seq("customer_id"))
      .groupBy("region").agg(sum("amount").as("revenue")).orderBy("region")

  def skewFacts(spark: SparkSession): DataFrame =
    spark.range(1000).select(col("id").as("event_id"),
      when(col("id") < 900, lit("hot"))
        .otherwise(concat(lit("cold-"), (col("id") % 10).cast("string"))).as("key"),
      ((col("id") % 7) + 1).as("amount"))

  def saltedTotals(facts: DataFrame, buckets: Int = 8): DataFrame = {
    require(buckets > 0, "buckets must be positive")
    facts.withColumn("salt",
      when(col("key") === "hot", pmod(xxhash64(col("event_id")), lit(buckets)))
        .otherwise(lit(0)))
      .groupBy("key", "salt").agg(sum("amount").as("subtotal"))
      .groupBy("key").agg(sum("subtotal").as("total"))
  }

  def requireEqual(left: DataFrame, right: DataFrame): Unit = {
    requireNoRows(left.exceptAll(right), "Unexpected extra output rows")
    requireNoRows(right.exceptAll(left), "Missing output rows")
  }
}
