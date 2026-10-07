package interviewprep.scalabasics

import scala.util.{Failure, Success, Try}

/** Typed errors preserve business meaning; Option alone cannot explain failure. */
object AlgebraicDataTypes:
  enum InputError:
    case NotAnInteger(raw: String)
    case OutOfRange(value: Int)

  enum Decision:
    case Accepted(score: Int)
    case Rejected(reason: String)

  def score(raw: String): Either[InputError, Int] =
    for
      value <- raw.toIntOption.toRight(InputError.NotAnInteger(raw))
      valid <- if value >= 0 && value <= 100 then Right(value)
               else Left(InputError.OutOfRange(value))
    yield valid

  def decide(raw: String): Either[InputError, Decision] =
    score(raw).map { value =>
      if value >= 70 then Decision.Accepted(value)
      else Decision.Rejected("practice fundamentals")
    }

  def render(decision: Decision): String = decision match
    // No catch-all: adding a new enum case produces an exhaustivity warning.
    case Decision.Accepted(value) => s"accepted: $value"
    case Decision.Rejected(reason) => s"rejected: $reason"

  def fromLegacy(raw: String): Either[InputError, Int] =
    // Try captures NonFatal exceptions at an impure/legacy boundary, not every Throwable.
    Try(Integer.parseInt(raw)) match
      case Success(value) => Right(value)
      case Failure(_: NumberFormatException) => Left(InputError.NotAnInteger(raw))
      case Failure(unexpected) => throw unexpected

  def main(args: Array[String]): Unit =
    Checks.equal(Right(85), score("85"), "valid score")
    Checks.equal(Right(0), score("0"), "inclusive lower bound")
    Checks.equal(Right(100), score("100"), "inclusive upper bound")
    Checks.equal(Left(InputError.NotAnInteger("bad")), score("bad"), "parse failure retained")
    Checks.equal(Left(InputError.OutOfRange(101)), score("101"), "domain failure retained")
    Checks.equal(Right(Decision.Rejected("practice fundamentals")), decide("69"),
      "valid input may still produce a business rejection")
    Checks.equal(Right("accepted: 70"), decide("70").map(render), "decision boundary")
    Checks.equal(Left(InputError.NotAnInteger("x")), fromLegacy("x"), "exception translation")

    var visited = false
    val failed: Either[InputError, Int] = Left(InputError.OutOfRange(-1))
    val skipped = failed.flatMap { value =>
      visited = true
      Right(value + 1)
    }
    Checks.equal(failed, skipped, "Either is right-biased and short-circuits")
    Checks.that(!visited, "later validations do not run after a Left")
    Checks.equal(None, score("bad").toOption, "toOption intentionally discards error details")
    println("AlgebraicDataTypes passed: total matching, typed errors, short-circuiting.")
