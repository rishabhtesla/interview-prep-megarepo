package course

import org.apache.spark.sql.functions._

/** Dependency-free executable assertions, submitted with the same runtime as the labs. */
object SmokeChecks {
  def main(args: Array[String]): Unit =
    Course.run("course-smoke-checks", args) { (spark, root) =>
      val counts = Course.wordCounts(spark.read.text(s"$root/lines.txt")).collect()
      assert(counts.head.getString(0) == "spark" && counts.head.getLong(1) == 3L)
      assert(counts.map(_.getLong(1)).sum == 10L)
      val (orders, customers) = Course.loadSales(spark, root)
      val rows = Course.latestCustomerSales(orders, customers).collect()
      assert(rows.map(_.getString(2)).toSeq == Seq("o2", "o5", "o6"))
      assert(rows.map(_.getDecimal(4).toPlainString).toSeq == Seq("30.00", "40.00", "15.00"))
      assert(Course.regionTotals(orders, customers).collect()
        .map(_.getDecimal(1).toPlainString).toSeq == Seq("40.00", "45.00"))
      val facts = Course.skewFacts(spark)
      Course.requireEqual(facts.groupBy("key").agg(sum("amount").as("total")),
        Course.saltedTotals(facts))
      assert(facts.agg(sum("amount")).first().getLong(0) == 3997L)
      val invalid = Course.readStrings(spark, s"$root/invalid_orders.csv", Course.orderColumns)
      var rejected = false
      try Course.cleanOrders(invalid)
      catch { case e: IllegalArgumentException if e.getMessage.contains("Invalid order") =>
        rejected = true
      }
      assert(rejected, "Invalid records must not be silently discarded")
      println("Scala smoke checks passed")
    }
}
