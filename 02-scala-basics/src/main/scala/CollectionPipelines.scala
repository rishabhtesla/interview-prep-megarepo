package interviewprep.scalabasics

/** Immutable collections, flattening and folds with explicit ordering and empty-input rules. */
object CollectionPipelines:
  def parseNonNegative(raw: String): Option[Int] =
    raw.toIntOption.filter(_ >= 0)

  def histogram(values: List[String]): Map[String, Int] =
    values.foldLeft(Map.empty[String, Int]) { (counts, value) =>
      // updated creates a new persistent map, sharing unaffected internal structure.
      counts.updated(value, counts.getOrElse(value, 0) + 1)
    }

  def main(args: Array[String]): Unit =
    val inputs = List("4", "bad", "-1", "2", "4")
    val parsed: List[Option[Int]] = inputs.map(parseNonNegative)
    val valid: List[Int] = inputs.flatMap(parseNonNegative)
    Checks.equal(List(Some(4), None, None, Some(2), Some(4)), parsed,
      "map preserves one output slot per input")
    Checks.equal(List(4, 2, 4), valid, "flatMap combines zero-or-one outputs")
    Checks.equal(List(4, 4), valid.filter(_ > 2), "filter preserves passing elements")
    Checks.equal(10, valid.reduce(_ + _), "reduce works on a known nonempty collection")
    Checks.equal(None, List.empty[Int].reduceOption(_ + _), "safe empty reduction")
    Checks.equal(0, List.empty[Int].foldLeft(0)(_ + _), "fold defines the empty case")
    Checks.equal(-10, valid.foldLeft(0)(_ - _), "left fold has an explicit evaluation order")
    Checks.equal(6, valid.foldRight(0)(_ - _), "right association changes subtraction")
    Checks.equal(Map("java" -> 2, "scala" -> 1),
      histogram(List("java", "scala", "java")), "frequency map")
    Checks.equal(Map.empty[String, Int], histogram(Nil), "empty histogram")

    val cartesian = for
      letter <- List("a", "b")
      number <- List(1, 2)
      if number % 2 == 0
    yield s"$letter$number"
    Checks.equal(List("a2", "b2"), cartesian,
      "for over List desugars to flatMap, withFilter and map, not zip")
    Checks.equal(List(("a", 1), ("b", 2)), List("a", "b").zip(List(1, 2)),
      "zip pairs positions instead of producing all combinations")

    var visits = 0
    val deferred = inputs.view.map { text =>
      visits += 1
      text.length
    }
    Checks.equal(0, visits, "view defers transformations")
    Checks.equal(List(1, 3), deferred.take(2).toList, "terminal materialization forces a prefix")
    Checks.equal(2, visits, "only requested elements are evaluated")
    deferred.take(1).toList
    Checks.equal(3, visits, "views recompute; they are not memoized results")
    Checks.equal(Vector(4, 2, 4), valid.toVector, "choose representation for needed operations")
    println("CollectionPipelines passed: map/flatMap, folds, histogram, lazy view.")
