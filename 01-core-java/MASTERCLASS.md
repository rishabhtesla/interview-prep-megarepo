# Core Java zero-to-hero masterclass

This is the beginner-first route through the Java track. It is designed for
someone who can read basic code but wants to answer backend interview questions
with precise reasoning rather than memorised definitions.

The rule for every topic is:

```text
understand -> run -> inspect -> change -> test -> explain
```

The executable tour is intentionally compact. The existing lessons then provide
the deeper JVM, collections, concurrency, functional programming, Java 17, and
testing treatment.

## 0. Setup and the first run

Use JDK 17 or a newer JDK that supports `--release 17`:

```bash
cd 01-core-java
javac --release 17 -Xlint:all -Werror -d out \
  src/main/java/com/interviewprep/core/*.java
java -cp out com.interviewprep.core.ZeroToHero
```

Expected final line:

```text
Zero-to-hero Java tour passed.
```

If compilation fails, fix the toolchain before studying the code. The program
uses explicit checks, so it does not depend on enabling Java assertions.

## 1. Read the guided executable

Open [`ZeroToHero.java`](src/main/java/com/interviewprep/core/ZeroToHero.java)
and read one method at a time:

| Method | First principle | What to say aloud |
|---|---|---|
| `basics` | Variables hold values; expressions produce values; control flow chooses repetition and branches | “What is stored, what is copied, and which branch executes?” |
| `arraysAndStrings` | Arrays are fixed-size mutable containers; `String` is immutable | “Which operation creates a new object?” |
| `objectDesign` | Records model values; interfaces isolate a policy; composition delegates behavior | “What can change without changing the caller?” |
| `collectionsAndGenerics` | Choose a collection from its contract; generic bounds communicate read/write direction | “Why does `? extends` produce and `? super` consume?” |
| `exceptionsAndValidation` | A failure contract is part of an API | “What input is invalid, and where is that fact reported?” |
| `streamsAndOptional` | A stream is a one-use lazy pipeline; `Optional` makes absence explicit | “What is lazy, what is terminal, and what happens when nothing matches?” |
| `concurrency` | Tasks run independently; futures deliver results; atomics protect one shared update | “Who owns the executor, and what is guaranteed to be atomic?” |
| `interviewPattern` | A hash map trades memory for expected constant-time lookup | “What invariant does the map maintain at each loop iteration?” |

After each method, change exactly one thing. Examples:

1. Change the tax percentage and predict the total.
2. Add a name to the array and explain why the loop still works.
3. Reject a zero-price product and add a check for it.
4. Replace `ArrayDeque` with a stack operation and explain the ordering change.
5. Make the score invalid and identify the exception boundary.
6. Remove `orElse` and explain why the absence case is no longer handled.
7. Remove `executor.shutdown()` and explain why a small program can still leak a
   non-daemon worker.
8. Insert a duplicate two-sum value and state why the earlier index remains
   valid.

Do not move forward until you can explain both the expected output and the
reason for each explicit check.

## 2. Deepen each concept with the existing lessons

| Foundation concept | Deeper lesson | Interview question |
|---|---|---|
| Values and references | [`JvmAndValues`](src/main/java/com/interviewprep/core/JvmAndValues.java) | Is Java pass-by-reference? |
| Primitive/boxed types and exceptions | [`TypesGenericsExceptions`](src/main/java/com/interviewprep/core/TypesGenericsExceptions.java) | Why can `Integer` unboxing throw? |
| Interfaces and value objects | [`ObjectDesign`](src/main/java/com/interviewprep/core/ObjectDesign.java) | What must `equals` and `hashCode` guarantee? |
| Collection contracts | [`CollectionChoices`](src/main/java/com/interviewprep/core/CollectionChoices.java) | When is `ArrayDeque` preferable to `LinkedList`? |
| Threads and memory visibility | [`ThreadsAndVisibility`](src/main/java/com/interviewprep/core/ThreadsAndVisibility.java) | Why does `volatile` not make `count++` atomic? |
| Futures and lifecycle | [`ExecutorsAndFutures`](src/main/java/com/interviewprep/core/ExecutorsAndFutures.java) | Who handles cancellation and shutdown? |
| Locks and concurrent maps | [`LocksAndConcurrentMaps`](src/main/java/com/interviewprep/core/LocksAndConcurrentMaps.java) | How do you prevent lock-order deadlocks? |
| Streams and absence | [`FunctionalPipelines`](src/main/java/com/interviewprep/core/FunctionalPipelines.java) | When is a loop clearer than a stream? |
| Java 17 language features | [`ModernJava17`](src/main/java/com/interviewprep/core/ModernJava17.java) | Record versus entity: what is the identity model? |
| Testing and performance | [`TestingAndPerformance`](src/main/java/com/interviewprep/core/TestingAndPerformance.java) | What does an exhaustive small-domain test not prove? |

Run the aggregate after each deeper lesson:

```bash
java -cp out com.interviewprep.core.CoreJavaDemo
```

The runner now executes the beginner tour followed by all ten specialised
lessons and ends with `All 11 Java lessons passed.`.

## 3. Interview-ready revision card

Before an interview, be able to explain these without opening the repository:

```text
primitive value vs reference value
mutable object vs final reference
String vs StringBuilder
record value equality vs entity identity
interface polymorphism vs inheritance
List vs Set vs Map vs Deque
? extends producer vs ? super consumer
checked/unchecked failure contracts
map lookup invariant for two-sum
stream intermediate operation vs terminal operation
Optional absence vs null
visibility vs atomicity vs ordering
executor ownership and shutdown
time complexity vs space complexity
```

## 4. Completion checkpoint

You are ready for the Spring and Spark tracks when you can:

- implement a small immutable value object with validation;
- choose a collection and state its dominant operation and complexity;
- explain why a `HashMap` solution is expected `O(n)` and what extra space it
  uses;
- describe a happens-before edge and distinguish it from atomicity;
- write a test for a boundary and explain what the test does not guarantee;
- compile the track with `-Xlint:all -Werror` and make all checks pass.
