package interviewprep.scalabasics

/** Case classes supply value semantics and extraction, not automatic deep immutability. */
object CaseClassesAndMatching:
  case class Candidate(name: String, score: Int):
    require(name.nonEmpty, "name is required")
    require(score >= 0 && score <= 100, "score must be between 0 and 100")

  def feedback(candidate: Candidate): String = candidate match
    case Candidate(name, score) if score >= 85 => s"$name: strong"
    case Candidate(name, score) if score >= 70 => s"$name: developing"
    case Candidate(name, _) => s"$name: practice"

  def firstTwo[A](values: List[A]): Option[(A, A)] = values match
    case first :: second :: _ => Some((first, second))
    case _ => None // Covers both empty and singleton lists, avoiding MatchError.

  def main(args: Array[String]): Unit =
    val ada = Candidate("Ada", 84)
    val improved = ada.copy(score = 90)
    Checks.equal(84, ada.score, "copy does not mutate the original")
    Checks.equal(Candidate("Ada", 90), improved, "generated equals uses constructor fields")
    Checks.equal(Candidate("Ada", 90).hashCode, improved.hashCode,
      "equal case classes have equal hashes")
    Checks.equal("Ada: developing", feedback(ada), "guard selects the middle branch")
    Checks.equal("Ada: strong", feedback(improved), "higher-priority guard wins")
    Checks.equal("Lin: practice", feedback(Candidate("Lin", 0)), "total fallback")
    Checks.equal(Some((1, 2)), firstTwo(List(1, 2, 3)), "List extractor")
    Checks.equal(None, firstTwo(List(1)), "singleton has no pair")
    Checks.equal(None, firstTwo(List.empty[Int]), "empty input has no pair")

    // copy calls the constructor and therefore rechecks its invariants.
    val invalidCopy = scala.util.Try(ada.copy(score = 101))
    Checks.that(invalidCopy.isFailure, "copy cannot bypass require")
    val namesByScore = Map(ada -> "original", improved -> "improved")
    Checks.equal(Some("original"), namesByScore.get(Candidate("Ada", 84)),
      "case class can be a stable map key when all relevant fields are immutable")
    println("CaseClassesAndMatching passed: value semantics, guards, total matching.")
