package interviewprep.scalabasics

/** Course runner. Each lesson can also be selected with --main-class. */
object InterviewPrep:
  def main(args: Array[String]): Unit =
    ValuesAndFunctions.main(args)
    CaseClassesAndMatching.main(args)
    CollectionPipelines.main(args)
    ClassesAndTraits.main(args)
    AlgebraicDataTypes.main(args)
    ContextualParameters.main(args)
    FuturesAndEffects.main(args)
    println("All 7 Scala lessons passed.")
