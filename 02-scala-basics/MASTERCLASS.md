# Scala 3 zero-to-hero masterclass

This track teaches the language from first principles, then connects syntax to
the interview topics that matter in production: immutable data, collection
complexity, typed failures, contextual abstraction, Java interoperability, and
asynchronous ownership.

The study loop is:

```text
understand -> run -> inspect -> change -> test -> explain
```

## Important version boundary

This directory is **Scala 3.3.6** and uses Scala CLI. The Spark directory has a
separate **Scala 2.12.18** sbt build because Spark 3.5.6 artifacts are published
for that binary version. Do not copy Scala 3 indentation, enums, or `given` code
into the Scala 2.12 Spark source without translating it.

## 0. Setup and first run

From this directory:

```bash
scala-cli compile src/main/scala --server=false
scala-cli run src/main/scala --server=false \
  --main-class interviewprep.scalabasics.ZeroToHero
```

Expected final line:

```text
Zero-to-hero Scala tour passed.
```

If Scala CLI is not installed, install it using the official Scala CLI
instructions, then rerun the commands. The source pins the compiler and JDK
requirements in [`project.scala`](src/main/scala/project.scala).

## 1. Read the guided executable

Open [`ZeroToHero.scala`](src/main/scala/ZeroToHero.scala) and study the
methods in this order:

| Method | Concept | Explain it in an interview |
|---|---|---|
| `valuesAndFunctions` | `val`, `var`, expressions, function values | Why does an expression-oriented style reduce mutable state? |
| `collections` | `map`, `filter`, `flatMap`, `distinct`, `zipWithIndex` | What is the shape of the collection after each operation? |
| `domainModeling` | case classes, `copy`, enums, exhaustive matching | How does the type make invalid states harder to represent? |
| `errorsAndEffects` | `Option`, `Either`, safe parsing, for-comprehensions | When should absence, a domain error, or an exception be used? |
| `typeClassesAndInterop` | `given`/`using`, type classes, Java APIs | How is behavior supplied without changing the data type? |
| `concurrency` | eager `Future`, `ExecutionContext`, `Await`, shutdown | Where does work run, and who owns its resources? |

For each section:

1. Predict the value checked by `Checks.equal`.
2. Run only the object with `--main-class`.
3. Change one input or branch.
4. Recompile with warnings as errors.
5. Explain the changed result and the trade-off.

Examples:

- Change `distinct` to `toSet` and explain what ordering information is lost.
- Add a new `ProgressStatus` case and let exhaustive matching show every place
  that needs an update.
- Replace `Either` with an exception and explain how the caller's contract
  becomes less visible.
- Define two `given Render[Learner]` values and explain the ambiguous-given
  compiler error.
- Change the executor size and explain why it changes concurrency capacity, not
  the correctness of the sum.

## 2. Continue with the deeper lessons

| Foundation | Deeper source | Checkpoint |
|---|---|---|
| Values and by-name evaluation | [`ValuesAndFunctions.scala`](src/main/scala/ValuesAndFunctions.scala) | Explain laziness versus memoization |
| Case classes and matching | [`CaseClassesAndMatching.scala`](src/main/scala/CaseClassesAndMatching.scala) | Explain equality and total matches |
| Collections and folds | [`CollectionPipelines.scala`](src/main/scala/CollectionPipelines.scala) | Explain `foldLeft` on empty input |
| Traits and composition | [`ClassesAndTraits.scala`](src/main/scala/ClassesAndTraits.scala) | Explain subtype polymorphism versus composition |
| Algebraic data types | [`AlgebraicDataTypes.scala`](src/main/scala/AlgebraicDataTypes.scala) | Explain `Option`, `Either`, and `Try` boundaries |
| Type classes | [`ContextualParameters.scala`](src/main/scala/ContextualParameters.scala) | Explain contextual lookup and explicit override |
| Effects and futures | [`FuturesAndEffects.scala`](src/main/scala/FuturesAndEffects.scala) | Explain eager work, callbacks, failure, and executor ownership |

Run the aggregate when the individual lessons pass:

```bash
scala-cli run src/main/scala --server=false \
  --main-class interviewprep.scalabasics.InterviewPrep
```

It ends with `All 8 Scala lessons passed.`.

## 3. Revision card

```text
val binding vs immutable object
expression result vs statement side effect
map vs flatMap
List vs Vector and collection shape
case-class equality/copy/extraction
sealed/enum variants and exhaustive matching
Option absence vs Either domain failure vs exception
for-comprehension as flatMap/map sequencing
trait/subtype polymorphism vs type-class behavior
given/using resolution and ambiguity
Future eager execution vs lazy description
ExecutionContext capacity vs correctness
resource ownership and shutdown
Scala 3 syntax vs Scala 2.12 Spark compatibility
```

## 4. Completion checkpoint

Move to the Spark Scala source when you can implement a pure function from
`String` to `Either[String, Result]`, explain every collection shape in a
pipeline, write an exhaustive match for a closed domain, and describe exactly
which executor owns a `Future` callback.
