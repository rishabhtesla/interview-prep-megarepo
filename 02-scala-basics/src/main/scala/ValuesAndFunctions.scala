package interviewprep.scalabasics

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

/** Expressions, function values, evaluation strategy and stack-safe recursion. */
object ValuesAndFunctions:
  def classify(score: Int): String =
    // An if expression returns a value; no mutable result placeholder is needed.
    if score >= 85 then "strong"
    else if score >= 70 then "developing"
    else "practice"

  def transformTwice(value: Int, f: Int => Int): Int = f(f(value))

  def choose[A](usePrimary: Boolean, primary: => A, fallback: => A): A =
    // By-name defers an expression, but DOES NOT memoize repeated evaluations.
    if usePrimary then primary else fallback

  def factorial(n: Int): BigInt =
    require(n >= 0, "factorial requires a non-negative integer")
    @tailrec
    def loop(remaining: Int, accumulated: BigInt): BigInt =
      if remaining == 0 then accumulated
      else loop(remaining - 1, accumulated * remaining)
    loop(n, BigInt(1))

  def main(args: Array[String]): Unit =
    val increment: Int => Int = _ + 1
    Checks.equal(5, transformTwice(3, increment), "functions are ordinary typed values")
    Checks.equal("strong", classify(85), "inclusive upper classification boundary")
    Checks.equal("developing", classify(70), "inclusive lower classification boundary")
    Checks.equal("practice", classify(69), "below boundary")
    Checks.equal(BigInt(1), factorial(0), "empty product is one")
    Checks.equal(BigInt(120), factorial(5), "accumulator recursion")

    var evaluations = 0
    def next(): Int =
      evaluations += 1
      evaluations
    Checks.equal(7, choose(true, 7, next()), "unselected by-name branch is not evaluated")
    Checks.equal(0, evaluations, "fallback has not run")
    def twice(value: => Int): Int = value + value
    Checks.equal(3, twice(next()), "by-name expression is evaluated on every access")
    lazy val cached = next()
    Checks.equal(6, twice(cached), "lazy val memoizes the first successful evaluation")
    Checks.equal(3, evaluations, "memoized expression evaluated once")

    val buffer = ArrayBuffer(1)
    buffer += 2
    Checks.equal(List(1, 2), buffer.toList, "val fixes a reference, not the referenced object")
    // A List is persistent: adding a head returns a new list and shares the old tail.
    val original = List(2, 3)
    val extended = 1 :: original
    Checks.equal(List(2, 3), original, "immutable input unchanged")
    Checks.equal(List(1, 2, 3), extended, "new value represents the update")
    println("ValuesAndFunctions passed: expressions, evaluation, recursion, immutability.")
