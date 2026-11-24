package course

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

/** Beginner-first Spark tour using the same execution model as the PySpark lesson. */
object ZeroToHero {
  def main(args: Array[String]): Unit = Course.run("spark-zero-to-hero", args) { (spark, _) =>
    import spark.implicits._

    println("\n1) Create a DataFrame with an explicit shape")
    val orders = Seq(
      ("o1", "c1", "java", 1200, "2025-01-01 09:00:00"),
      ("o2", "c1", "spring", 2000, "2025-01-02 09:00:00"),
      ("o3", "c2", "spark", 1500, "2025-01-01 10:00:00"),
      ("o4", "c2", "java", 900, "2025-01-03 10:00:00")
    ).toDF("order_id", "customer_id", "topic", "amount_cents", "event_time")
    val customers = Seq(
      ("c1", "Ada", "US"),
      ("c2", "Grace", "GB")
    ).toDF("customer_id", "name", "region")
    orders.printSchema()

    println("\n2) Compose lazy transformations")
    val typedOrders = orders
      .withColumn("event_ts", to_timestamp(col("event_time"), "yyyy-MM-dd HH:mm:ss"))
      .withColumn("amount", (col("amount_cents") / lit(100)).cast("decimal(10,2)"))
    val javaOrders = typedOrders.filter(col("topic") === "java")
      .select("order_id", "customer_id", "amount")

    // The small dimension is broadcast so the fact side need not be shuffled for this join.
    val joined = typedOrders.join(broadcast(customers), Seq("customer_id"), "inner")
    val byCustomer = Window.partitionBy("customer_id")
    val newestFirst = byCustomer.orderBy(col("event_ts").desc, col("order_id").desc)
    val report = joined
      .withColumn("lifetime_cents", sum("amount_cents").over(byCustomer))
      .withColumn("position", row_number().over(newestFirst))
      .filter(col("position") === 1)
      .select(
        "customer_id",
        "name",
        "region",
        "order_id",
        "amount",
        "lifetime_cents"
      )
      .orderBy("customer_id")

    // An action executes the plan. Cache only for a reused result and release it afterwards.
    report.cache()
    report.count()
    report.show(false)
    javaOrders.show(false)

    println("\n3) Read the physical plan")
    report.explain("formatted")

    val actual = report.collect().map(row =>
      (row.getAs[String]("customer_id"),
        row.getAs[String]("order_id"),
        row.getAs[Long]("lifetime_cents"))
    ).toSeq
    require(
      actual == Seq(("c1", "o2", 3200L), ("c2", "o4", 2400L)),
      s"latest-order contract failed: $actual"
    )
    require(javaOrders.count() == 2, "filter contract failed")
    println(s"partitions before explicit repartition: ${orders.rdd.getNumPartitions}")
    println(
      s"partitions after explicit repartition: " +
        s"${orders.repartition(col("customer_id")).rdd.getNumPartitions}"
    )
    report.unpersist()
    println("Zero-to-hero Scala Spark tour passed.")
  }
}
