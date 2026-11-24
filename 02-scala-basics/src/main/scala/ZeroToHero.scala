package interviewprep.scalabasics

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

/**
 * A first executable tour of Scala 3.
 *
 * Read one section, predict the checks, run the object, and then change one
 * expression. The existing lessons explain each topic in more depth.
 */
object ZeroToHero:
  def main(args: Array[String]): Unit =
    valuesAndFunctions()
    collections()
    domainModeling()
    errorsAndEffects()
    typeClassesAndInterop()
    concurrency()
    println("Zero-to-hero Scala tour passed.")

  private def valuesAndFunctions(): Unit =
    println("\n1) Values, expressions, and functions")

    // `val` is an immutable binding. An if expression returns a value.
    val score = 82
    val label =
      if score >= 85 then "strong"
      else if score >= 70 then "developing"
      else "practice"

    // Function values have types. `map` can receive this function as data.
    val double: Int => Int = value => value * 2
    val result = double(21)
    Checks.equal("developing", label, "conditional expression")
    Checks.equal(42, result, "function value")

    // `var` permits reassignment; prefer `val` and return a new value when possible.
    var attempts = 0
    attempts += 1
    Checks.equal(1, attempts, "local mutable counter")

  private def collections(): Unit =
    println("\n2) Immutable collections and transformations")

    val rawTopics = List("java", "scala", "java", "spark")
    val distinctTopics = rawTopics.distinct
    val upperCaseTopics = distinctTopics.map(_.toUpperCase)
    val longTopics = upperCaseTopics.filter(_.length >= 5)
    val indexedTopics = upperCaseTopics.zipWithIndex

    // `flatMap` maps and then removes one collection nesting level.
    val words = List("java spark", "scala")
      .flatMap(_.split(" ").toList)

    Checks.equal(List("java", "scala", "spark"), distinctTopics, "distinct values")
    Checks.equal(List("JAVA", "SCALA", "SPARK"), upperCaseTopics, "map transforms")
    Checks.equal(List("SCALA", "SPARK"), longTopics, "filter keeps matching values")
    Checks.equal(List("java", "spark", "scala"), words, "flatMap flattens")
    Checks.equal((("JAVA", 0)), indexedTopics.head, "zip adds position")

  private def domainModeling(): Unit =
    println("\n3) Case classes, enums, pattern matching, and traits")

    val learner = Learner("Ada", Set("java", "scala"), LocalDate.of(2025, 1, 1))
    val updated = learner.copy(topics = learner.topics + "spark")
    val status = progressStatus(updated.topics.size)

    // A case class supplies useful equality, copy, and readable pattern matching.
    status match
      case ProgressStatus.Starting => Checks.that(false, "Ada should not be starting")
      case ProgressStatus.Building => Checks.that(false, "Ada should not be building")
      case ProgressStatus.Ready => Checks.equal(ProgressStatus.Ready, status, "enum match")

    Checks.equal(Set("java", "scala"), learner.topics, "case class is immutable")
    Checks.equal(Set("java", "scala", "spark"), updated.topics, "copy creates a new value")
    Checks.equal("Ada:3", describe(updated), "trait-style domain behavior")

  private final case class Learner(name: String, topics: Set[String], started: LocalDate)

  private enum ProgressStatus:
    case Starting
    case Building
    case Ready

  private def progressStatus(topicCount: Int): ProgressStatus =
    topicCount match
      case 0 => ProgressStatus.Starting
      case 1 | 2 => ProgressStatus.Building
      case _ => ProgressStatus.Ready

  private def describe(learner: Learner): String =
    s"${learner.name}:${learner.topics.size}"

  private def errorsAndEffects(): Unit =
    println("\n4) Option, Either, and for-comprehensions")

    // Option represents a value that may be absent; no null check is hidden.
    val firstTopic = List.empty[String].headOption.getOrElse("start-java")
    Checks.equal("start-java", firstTopic, "Option absence")

    // Either keeps a domain error as data. The for-comprehension stops at the first Left.
    val validAge = parseAge("30")
    val invalidAge = parseAge("-1")
    Checks.equal(Right(30), validAge, "valid Either result")
    Checks.equal(Left("age must be non-negative"), invalidAge, "invalid Either result")

    val parsedNumbers = List("1", "2", "bad").map(_.toIntOption)
    Checks.equal(List(Some(1), Some(2), None), parsedNumbers, "safe parsing")

  private def parseAge(text: String): Either[String, Int] =
    for
      age <- text.toIntOption.toRight("age must be an integer")
      _ <- Either.cond(age >= 0, (), "age must be non-negative")
    yield age

  private def typeClassesAndInterop(): Unit =
    println("\n5) Type classes, contextual values, and Java interoperability")

    given Render[Learner] = learner => s"${learner.name} knows ${learner.topics.size} topics"
    val learner = Learner("Grace", Set("java"), LocalDate.of(2025, 2, 1))
    Checks.equal("Grace knows 1 topics", render(learner), "contextual renderer")

    // Scala can call Java standard-library APIs directly.
    val date = LocalDate.of(2025, 2, 1)
    Checks.equal(2, date.getMonthValue, "Java LocalDate interoperability")

  private trait Render[A]:
    def render(value: A): String

  private def render[A](value: A)(using renderer: Render[A]): String =
    renderer.render(value)

  private def concurrency(): Unit =
    println("\n6) Futures, execution contexts, and ownership")

    // A Future starts its work eagerly. This executor is created and closed by this method.
    val executor = Executors.newFixedThreadPool(2)
    given ExecutionContext = ExecutionContext.fromExecutorService(executor)
    try
      val doubled = Future.sequence(List(1, 2, 3).map(value => Future(value * 2)))
      val total = Await.result(doubled.map(_.sum), 5.seconds)
      Checks.equal(12, total, "Future composition")
    finally
      executor.shutdown()
