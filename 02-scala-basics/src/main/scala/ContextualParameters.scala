package interviewprep.scalabasics

/** A type class adds behavior without requiring the domain type to inherit that behavior. */
object ContextualParameters:
  case class Candidate(name: String, score: Int)

  trait Show[A]:
    def show(value: A): String

  object Show:
    // Instances in the type class companion participate in contextual implicit scope.
    given Show[Int] with
      def show(value: Int): String = value.toString

    given Show[Candidate] with
      def show(value: Candidate): String = s"${value.name}:${value.score}"

    given [A](using element: Show[A]): Show[List[A]] with
      def show(values: List[A]): String =
        values.map(element.show).mkString("[", ", ", "]")

  def render[A](value: A)(using instance: Show[A]): String = instance.show(value)

  extension [A](value: A)
    def shown(using instance: Show[A]): String = instance.show(value)

  // Context bounds are shorthand for requesting evidence, not runtime reflection.
  def renderAll[A: Show](values: List[A]): List[String] =
    values.map(value => summon[Show[A]].show(value))

  def main(args: Array[String]): Unit =
    val ada = Candidate("Ada", 90)
    Checks.equal("Ada:90", render(ada), "instance found through contextual scope")
    Checks.equal("[1, 2, 3]", render(List(1, 2, 3)), "list instance requires element evidence")
    Checks.equal("[]", render(List.empty[Int]), "empty list representation")
    Checks.equal("Ada:90", ada.shown, "extension method uses the same evidence")
    Checks.equal(List("1", "2"), renderAll(List(1, 2)), "context bound and summon")

    val redacted = new Show[Candidate]:
      def show(value: Candidate): String = s"<redacted>:${value.score}"
    Checks.equal("<redacted>:90", render(ada)(using redacted),
      "explicit using makes policy selection visible at a sensitive call site")
    Checks.equal("Ada:90", render(ada), "explicit override does not mutate the default instance")
    // Two equally eligible imported givens would be a compile-time ambiguity, not a
    // runtime coin toss. Prefer small scopes and explicit using at policy boundaries.
    println("ContextualParameters passed: givens, using, extensions, derived evidence.")
