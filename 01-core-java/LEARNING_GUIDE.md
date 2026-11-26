# Core Java learning guide: read, run, change, explain

This is the foundation for Spring, Kafka clients, Spark jobs, and backend
interviews. Complete the lessons in order. Each lesson has a runnable source
file and a question you must answer before moving on. Start with the executable
tour in [MASTERCLASS.md](MASTERCLASS.md), then use the deeper lessons below.

## Setup once

```bash
cd 01-core-java
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
javac --version
```

Run all lessons:

```bash
javac --release 17 -Xlint:all -Werror -d out src/main/java/com/interviewprep/core/*.java
java -cp out com.interviewprep.core.CoreJavaDemo
```

The aggregate runner should end with `All 11 Java lessons passed.` These are
deterministic educational checks, not a JUnit suite, production load test, or
proof of every possible thread schedule.

## First pass

Run [`ZeroToHero`](src/main/java/com/interviewprep/core/ZeroToHero.java) before
the deeper lessons. Read one section, predict its checks, run it, change one
line, and rerun:

```bash
java -cp out com.interviewprep.core.ZeroToHero
```

The tour covers values, control flow, arrays, strings, records, interfaces,
collections, generics, exceptions, streams, `Optional`, executors, atomics,
and a hash-map interview pattern.

## Lesson order

| Step | Read and run | What the code does | Change exercise | Interview checkpoint |
|---:|---|---|---|---|
| 1 | `ZeroToHero` / [MASTERCLASS.md](MASTERCLASS.md) | Tours syntax, arrays, objects, collections, exceptions, streams, concurrency, and two-sum | Change one section, predict the new check, and rerun | Explain the changed behavior without reading the code |
| 2 | [JVM and values](COURSE.md#1-jvm-execution-memory-and-pass-by-value) / `JvmAndValues` | Shows bytecode execution ideas, class initialization, references, and pass-by-value | Reassign a method parameter and then mutate the referenced list; predict which change the caller sees | Explain why Java is always pass-by-value |
| 3 | `TypesGenericsExceptions` | Demonstrates overflow, boxing, PECS, array covariance, and suppressed exceptions | Replace `Math.addExact` with ordinary addition and observe the contract change | Explain `List<Integer>` versus `List<? extends Number>` |
| 4 | `ObjectDesign` | Uses interfaces, composition, equality, and immutable IDs to enforce pricing contracts | Add a discount strategy without changing checkout orchestration | Explain `equals`/`hashCode` and Liskov substitution |
| 5 | `CollectionChoices` | Compares collection operations, ordering, views, and queue behavior | Replace an `ArrayDeque` with a `LinkedList`; explain the cost and contract change | Choose a collection from access and mutation requirements |
| 6 | `ThreadsAndVisibility` | Separates visibility from atomicity and demonstrates safe publication | Change an atomic increment into `count++`; explain lost updates | Explain happens-before and why `volatile` does not make `++` atomic |
| 7 | `ExecutorsAndFutures` | Submits tasks, observes failures, handles cancellation, and composes results | Remove executor shutdown and explain the resource leak | Explain ownership and lifecycle of an executor |
| 8 | `LocksAndConcurrentMaps` | Uses locks, ordered acquisition, and atomic map operations | Acquire locks in opposite order and identify the deadlock risk | Explain lock scope, deadlock prevention, and `ConcurrentHashMap.merge` |
| 9 | `FunctionalPipelines` | Demonstrates lambdas, lazy streams, grouping, and `Optional` | Add a second terminal operation and observe that a stream is one-shot | Explain when a loop is clearer than a stream |
| 10 | `ModernJava17` | Uses records, sealed types, pattern logic, and defensive copies | Pass a mutable list into a value object and then mutate the original | Explain records versus ordinary classes and sealed hierarchies |
| 11 | `TestingAndPerformance` | Runs boundary checks, overflow checks, and exhaustive small-domain comparisons | Add an adversarial input before changing the implementation | Explain what a test proves and what it does not prove |

## How to read one Java lesson

For `ThreadsAndVisibility`, use this exact order:

1. Read the definition of visibility, atomicity, and ordering.
2. Run `java -cp out com.interviewprep.core.ThreadsAndVisibility`.
3. Find the deliberately unsafe counter and the safe counter.
4. Explain what each worker reads and writes.
5. Change the number of workers and rerun.
6. Read the test assertion and state the invariant.
7. Answer: “Why would `volatile int count` still lose updates?”

Do not memorize a slogan such as “volatile is thread-safe.” State the exact
property it provides and the operation that remains non-atomic.

## Optional observations and version boundary

Run these only after the normal course works:

```bash
java -Xlog:class+load=info -cp out com.interviewprep.core.JvmAndValues
java -Xlog:gc -cp out com.interviewprep.core.CoreJavaDemo
javap -classpath out -c -p com.interviewprep.core.JvmAndValues
```

`--release 17` checks the Java 17 API surface. The course intentionally does not
use virtual threads, record patterns, or preview syntax. Run with an actual
Java 17 runtime when studying Java 17 GC/JIT behavior; a newer runtime only
proves that the class files execute there.

## Revision card

Before an interview, revise these contrasts:

```text
primitive value vs object reference value
mutable object vs immutable reference binding
equals/hashCode vs object identity
ArrayList vs LinkedList vs ArrayDeque
visibility vs atomicity vs mutual exclusion
Runnable execution vs Thread.start()
Future cancellation vs timeout
sequential stream vs parallel stream
record value semantics vs entity identity
test evidence vs production guarantee
```
