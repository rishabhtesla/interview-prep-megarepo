package course

object WordCount {
  def main(args: Array[String]): Unit =
    Course.run("course-word-count", args) { (spark, root) =>
      Course.wordCounts(spark.read.text(s"$root/lines.txt")).show(false)
    }
}
