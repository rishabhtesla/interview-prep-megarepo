# Scala 3 learning guide: from syntax to effects

Read this track after Core Java. The goal is to understand Scala 3
collections, algebraic data types, and effect boundaries without confusing
Scala 3 syntax with the Scala 2.12 Spark code in the next track. This directory
is pinned to Scala 3.3.6 and uses Scala CLI; the Spark module has a separate
Scala 2.12 build because that is the Spark 3.5.6 binary compatibility line.

## Setup

```bash
cd 02-scala-basics
scala-cli run src/main/scala --server=false \
  --main-class interviewprep.scalabasics.InterviewPrep
```

The exact pinned version and individual commands are in [README.md](README.md).
Run the source for one lesson, then read the matching section in
[COURSE.md](COURSE.md).

## Lesson order

| Step | Source | First question | Code action to observe | Change exercise |
|---:|---|---|---|---|
| 1 | [`ZeroToHero`](MASTERCLASS.md) / `ZeroToHero.scala` | What does this expression evaluate to? | Run values, collections, ADTs, errors, type classes, and futures | Change one expression and predict the check |
| 2 | `ValuesAndFunctions.scala` | Is this a value, a variable, or a function? | Evaluate expressions and pass a function as data | Replace `val` with `var`; explain what changed and what did not |
| 3 | `CaseClassesAndMatching.scala` | What variants can this input have? | Construct, copy, destructure, and match a case class | Add a fallback and explain what safety it hides |
| 4 | `CollectionPipelines.scala` | Does the transformation keep, remove, or expand elements? | Compare `map`, `flatMap`, `filter`, `fold`, and views | Replace `reduce` with `reduceOption` for empty input |
| 5 | `ClassesAndTraits.scala` | Which behavior belongs to a class and which is a role? | Compose traits and use an abstraction | Add a second implementation without changing the caller |
| 6 | `AlgebraicDataTypes.scala` | How are invalid states represented? | Model success/failure without `null` | Add a new variant and update every match |
| 7 | `ContextualParameters.scala` | Which implementation is supplied contextually? | Resolve contextual values/type-class behavior | Create two competing instances and explain the compiler error |
| 8 | `FuturesAndEffects.scala` | Where does asynchronous work run? | Compose futures, observe failure, and control execution context | Add a timeout and distinguish failure from cancellation |
| 9 | `InterviewPrep.scala` | Can I explain the trade-off? | Runs short checks across the track | Answer before reading the provided answer |

## The Scala execution order

For every example, identify:

```text
pure value construction
-> transformation
-> effect boundary
-> asynchronous completion
-> error representation
```

Scala syntax is not the main interview point. The important questions are
immutability, evaluation timing, collection complexity, type modeling, and
where side effects occur.

## Java interoperability checkpoint

Before moving to Spark, explain:

- `Option[A]` versus Java `null`.
- `Either[Error, Value]` versus throwing from every layer.
- `List.map` versus `List.flatMap`.
- A `Future` as a description of asynchronous completion, not a guarantee of
  parallel speedup.
- A trait as a reusable contract/implementation, not a Java class hierarchy.
