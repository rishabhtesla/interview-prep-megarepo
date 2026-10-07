# Core Java interview course — Java 17

A ten-lesson course in language semantics, design, collections, concurrency and
reasoning about correctness. This is **plain Java**, not Spring, a framework
tutorial, or an upgrade project. Every topic has a runnable program with explicit
self-checks; failed checks terminate with `AssertionError` even without `-ea`.

## Start here

Prerequisite: a JDK containing both `java` and `javac`. All code targets Java 17
without preview features or external dependencies. From **this directory**:

```bash
javac --release 17 -Xlint:all -Werror -d out src/main/java/com/interviewprep/core/*.java
java -cp out com.interviewprep.core.CoreJavaDemo
# Or choose a lesson:
java -cp out com.interviewprep.core.ThreadsAndVisibility
```

Successful aggregate execution ends with `All 10 Java lessons passed.` The last
lesson checks **42,966 deterministic input/target combinations** against an
independent reference implementation. These are educational executable checks,
not a JUnit suite, production load test, or proof of all possible thread schedules.

## Study progression

Read the [course](COURSE.md) in the order below. For each lesson: predict its
output, run it, explain why its checks pass, solve the corresponding
[exercise](EXERCISES.md) before reading the worked solution, then give a
two-minute answer aloud. Budget 60–90 minutes per lesson plus review; move at
your own pace rather than treating the estimate as a guarantee.

| Lesson / main class | Course focus | What the program verifies |
|---|---|---|
| [`JvmAndValues`](src/main/java/com/interviewprep/core/JvmAndValues.java) | JVM, GC, class loading, pass-by-value | Copied values, class identity/initialization, container references |
| [`TypesGenericsExceptions`](src/main/java/com/interviewprep/core/TypesGenericsExceptions.java) | Primitives, boxing, PECS, exceptions | Overflow, null unboxing, array covariance, suppressed failures |
| [`ObjectDesign`](src/main/java/com/interviewprep/core/ObjectDesign.java) | OOP, equality, interfaces, composition | Equality laws, map lookup, pricing contracts |
| [`CollectionChoices`](src/main/java/com/interviewprep/core/CollectionChoices.java) | Complexity, views, ordering | Counts, shallow snapshots, comparator uniqueness, deque behavior |
| [`ThreadsAndVisibility`](src/main/java/com/interviewprep/core/ThreadsAndVisibility.java) | Threads, Runnable, JMM, atomics | Deterministic lost update, correct atomic count, run versus start |
| [`ExecutorsAndFutures`](src/main/java/com/interviewprep/core/ExecutorsAndFutures.java) | Tasks, futures, lifecycle | Results, exception cause, timeout versus cancellation, composition |
| [`LocksAndConcurrentMaps`](src/main/java/com/interviewprep/core/LocksAndConcurrentMaps.java) | Monitors, locks, deadlock, concurrent maps | No overselling, conservation, ordered locks, atomic merge |
| [`FunctionalPipelines`](src/main/java/com/interviewprep/core/FunctionalPipelines.java) | Lambdas, streams, Optional | Flattening, grouping, eager/lazy fallback, one-shot streams |
| [`ModernJava17`](src/main/java/com/interviewprep/core/ModernJava17.java) | Records, sealed types, patterns | Defensive copy, value equality, validation, variant handling |
| [`TestingAndPerformance`](src/main/java/com/interviewprep/core/TestingAndPerformance.java) | Testing, debugging, tradeoffs | Boundary cases, overflow safety, exhaustive small-domain comparison |

`Checks.java` is shared assertion support and `CoreJavaDemo.java` is the runner,
not additional lessons.

## Revision and interview practice

1. **Foundation round:** explain lessons 1–4 without relying on a framework.
2. **Concurrency round:** draw happens-before edges and lock acquisition order
   for lessons 5–7. Explain ownership of every executor.
3. **API round:** justify streams versus loops, records versus ordinary classes.
4. **Mock coding round:** implement two-sum with overflow-safe arithmetic; give
   tests, complexity and a small-input tradeoff argument.
5. **Final rehearsal:** use the question/answer prompts in `COURSE.md`, then
   complete the mixed interview in `EXERCISES.md`.

## Optional observation commands

Run these only after normal execution works:

```bash
java -Xlog:class+load=info -cp out com.interviewprep.core.JvmAndValues
java -Xlog:gc -cp out com.interviewprep.core.CoreJavaDemo
javap -classpath out -c -p com.interviewprep.core.JvmAndValues
java -XX:StartFlightRecording=filename=course.jfr,dumponexit=true \
  -cp out com.interviewprep.core.CoreJavaDemo
```

Class-loading logs are verbose; a short run may show **no collection at all**.
The program deliberately does not assert GC timing, heap layout, allocation
elimination, or a specific default collector. JFR requires a JDK distribution
that supplies it, and a short recording is only an orientation exercise.

## Version and validation notes

- `--release 17` checks both language level and the Java 17 public API surface.
  Running those class files on a newer JDK does not establish Java 17 GC/JIT
  behavior; use an actual JDK 17 runtime when studying those details.
- No virtual threads (final in Java 21), record patterns, or pattern-switch
  preview flags are used. `ExecutorService` is **not** AutoCloseable in Java 17.
- Initial validation: strict compilation targeting 17 and all ten lessons passed
  on OpenJDK **26.0.2**. Java 17 runtime execution was not available in that check.
- Concurrency checks have bounded waits, observe worker failures and close
  executors. They are intentionally small; they are not stress/linearizability tests.
