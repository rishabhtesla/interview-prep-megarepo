package course

object SalesPipeline {
  def main(args: Array[String]): Unit =
    Course.run("course-sales-pipeline", args) { (spark, root) =>
      val (orders, customers) = Course.loadSales(spark, root)
      Course.latestCustomerSales(orders, customers).show(false)
    }
}
