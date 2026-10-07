package course

import org.apache.spark.storage.StorageLevel

object PerformancePlan {
  def main(args: Array[String]): Unit =
    Course.run("course-performance-plan", args) { (spark, root) =>
      val (orders, customers) = Course.loadSales(spark, root)
      val report = Course.regionTotals(orders, customers)
      report.explain("formatted")
      val cached = report.persist(StorageLevel.MEMORY_AND_DISK)
      try {
        println(s"Materialized regions: ${cached.count()}")
        cached.show(false)
        cached.explain("formatted")
      } finally cached.unpersist()
    }
}
