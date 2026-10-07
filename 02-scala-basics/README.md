# Scala interview foundations — Scala 3

Seven connected lessons covering Scala language fundamentals, immutable data,
functional error handling, contextual abstraction and asynchronous composition.
This track deliberately contains **no Spark** and uses no external libraries.

## Toolchain and execution

The shared [`project.scala`](src/main/scala/project.scala) Scala CLI directives
pin **Scala 3.3.6**, request **JDK 17**, and enable compiler warnings as errors.
Use Scala CLI; an arbitrary `scala` launcher, Scala 2 compiler or sbt invocation
does not necessarily honor these directives.

From **this directory**, with Scala CLI available:

```bash
# Compile all lessons, including the shared directives:
scala-cli compile src/main/scala --server=false

# Run every lesson and its explicit self-checks:
scala-cli run src/main/scala --server=false \
  --main-class interviewprep.scalabasics.InterviewPrep

# Run just one topic (still supply the directory for configuration/helpers):
scala-cli run src/main/scala --server=false \
  --main-class interviewprep.scalabasics.AlgebraicDataTypes
```

The first run may download the pinned compiler, Scala libraries and JDK if not
cached; network/proxy access to the appropriate repositories is required.
If you already have a suitable JDK 17 selected and want no managed-JDK lookup,
append `--jvm system` to these commands. Scala's compiler version stays pinned.

Expected aggregate success: `All 7 Scala lessons passed.` Each lesson has its
own `main` and uses explicit checks that throw `AssertionError`; these are
dependency-free self-checks, **not a ScalaTest/MUnit suite**. Use `run`, not
`scala-cli test`, to execute them.

## Learning path

Read [COURSE.md](COURSE.md) alongside each source file. Predict results before
execution, explain the types at every transformation, then attempt the
[exercises and worked solutions](EXERCISES.md). Plan roughly 60–90 minutes per
lesson plus interview rehearsal; Java knowledge is helpful but not required.

| Lesson / main object | Concepts | Self-check coverage |
|---|---|---|
| [`ValuesAndFunctions`](src/main/scala/ValuesAndFunctions.scala) | val/var, expressions, functions, by-name, recursion | Boundary classification, factorial, lazy versus repeated evaluation |
| [`CaseClassesAndMatching`](src/main/scala/CaseClassesAndMatching.scala) | Case classes, copy, extractors, guards | Equality/hash consistency, invariants, empty/singleton matching |
| [`CollectionPipelines`](src/main/scala/CollectionPipelines.scala) | map/flatMap/filter, reduce/folds, views | Empty cases, fold order, histogram, for versus zip, deferred work |
| [`ClassesAndTraits`](src/main/scala/ClassesAndTraits.scala) | Classes, traits, polymorphism, composition | Pricing boundaries, interchangeable formatter, invalid behavior |
| [`AlgebraicDataTypes`](src/main/scala/AlgebraicDataTypes.scala) | Enums/ADTs, Option/Either/Try, pure core | Typed failure causes, short-circuiting, domain boundaries |
| [`ContextualParameters`](src/main/scala/ContextualParameters.scala) | given/using, type classes, extensions | Evidence lookup, generic list instance, explicit policy override |
| [`FuturesAndEffects`](src/main/scala/FuturesAndEffects.scala) | Future/Promise, execution context, recovery | Eagerness, memoization, composition, order, executor cleanup |

`InterviewPrep.scala` is the aggregate runner; `Checks.scala` and `project.scala`
are infrastructure rather than additional lessons.

## What to be able to explain

- **First pass:** values versus mutable objects; function types; case-class
  extraction; List versus Vector; the empty fold result.
- **Second pass:** domain errors versus missing data versus thrown exceptions;
  total matches; type classes versus subtype polymorphism.
- **Third pass:** when Future work starts, where callbacks execute, why
  `flatMap` differs from `map`, and who closes the executor.
- **Mock round:** implement the validated batch-import exercise without `.get`,
  explain its inferred types, test empty/boundary inputs, then discuss bounded
  asynchronous execution.

## Version and validation boundaries

Scala 3 syntax here includes indentation, enums, `given`/`using`, extensions
and `import ...*`. The code is not advertised as Scala 2-compatible.
The standard-library collections follow the Scala 2.13 collection design used
by Scala 3. This track is separate from any Spark-specific Scala version.

At authoring time, the available environment had OpenJDK 26.0.2 but **no Scala
CLI or Scala compiler**, so Scala compilation and execution were not verified.
The commands above define the intended validation; do not treat the listed
expected output as a recorded successful run. Java course validation is
reported separately in that track.
