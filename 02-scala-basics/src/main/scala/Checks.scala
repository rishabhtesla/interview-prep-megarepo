package interviewprep.scalabasics

/** These explicit self-checks are not removed by compiler assertion-elision settings. */
private[scalabasics] object Checks:
  def equal[A](expected: A, actual: A, reason: String): Unit =
    if expected != actual then
      throw new AssertionError(s"$reason: expected $expected, got $actual")

  def that(condition: Boolean, reason: String): Unit =
    if !condition then throw new AssertionError(reason)
