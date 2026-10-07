package course

import org.apache.spark.sql.functions._

object SkewSalting {
  def main(args: Array[String]): Unit =
    Course.run("course-skew-salting", args) { (spark, _) =>
      val facts = Course.skewFacts(spark)
      val plain = facts.groupBy("key").agg(sum("amount").as("total"))
      val salted = Course.saltedTotals(facts)
      Course.requireEqual(plain, salted)
      salted.orderBy("key").show(20, false)
      val dimension = facts.select("key").distinct().withColumn("label", upper(col("key")))
      val expanded = dimension.withColumn("salt", explode(
        when(col("key") === "hot", sequence(lit(0), lit(7))).otherwise(array(lit(0)))))
      val saltedFacts = facts.withColumn("salt",
        when(col("key") === "hot", pmod(xxhash64(col("event_id")), lit(8))).otherwise(lit(0)))
      val joined = saltedFacts.join(expanded, Seq("key", "salt"))
        .select("event_id", "key", "amount", "label")
      val expected = facts.join(dimension, Seq("key"))
        .select("event_id", "key", "amount", "label")
      Course.requireEqual(expected, joined)
      println(s"Salted join preserves ${joined.count()} rows")
      saltedFacts.filter("key = 'hot'").groupBy("salt").count().orderBy("salt").show()
    }
}
