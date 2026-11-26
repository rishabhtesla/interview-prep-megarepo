# Concurrency, the Java Memory Model, and modern Java

## Mental model and flow

Concurrency has three separate questions:

```text
Visibility: will another thread see the write?
Atomicity: can the operation be observed halfway through?
Ordering: can operations be reordered without violating happens-before?
```

Run the lessons:

```bash
cd 01-core-java
javac --release 17 -Xlint:all -Werror -d out \
  src/main/java/com/interviewprep/core/*.java
java -cp out com.interviewprep.core.ThreadsAndVisibility
java -cp out com.interviewprep.core.ExecutorsAndFutures
java -cp out com.interviewprep.core.LocksAndConcurrentMaps
java -cp out com.interviewprep.core.FunctionalPipelines
```

## Interview questions and answers

### What are the Java thread states?

**Answer:** `NEW` has not started; `RUNNABLE` is eligible/running on a
platform thread; `BLOCKED` waits for a monitor; `WAITING` waits indefinitely
for another action; `TIMED_WAITING` waits with a timeout; `TERMINATED` has
completed. A thread dump shows the state and lock/wait relationship, which is
more useful than guessing from application symptoms.

### `synchronized` versus `Lock`, `ReadWriteLock`, and `StampedLock`?

**Answer:** `synchronized` is simple, reentrant, and automatically releases on
scope exit. `ReentrantLock` adds timed/interruptible acquisition and multiple
conditions but requires `unlock` in `finally`. `ReadWriteLock` can help
read-heavy workloads with low write contention. `StampedLock` offers optimistic
reads but is not reentrant and is easier to misuse. Use the simplest lock that
expresses ownership and measure before optimizing.

### What does `volatile` guarantee?

**Answer:** A volatile write happens-before a later read of the same field.
Readers see the latest volatile value and access is not cached/reordered past
the volatile boundary in ways that violate the JMM. `volatile` does not make
`count++` atomic: the read, add, and write are still separate operations. Use
an atomic type or a lock for read-modify-write.

### How do `AtomicInteger` and CAS work?

**Answer:** Compare-And-Swap reads the current value and writes the new value
only if the observed value is unchanged; a failed CAS retries. This avoids a
blocking lock for many short updates. ABA is the risk that a value changes A →
B → A, so a later CAS sees A and misses the intermediate change. Version
stamps or `AtomicStampedReference` address that when identity matters.

### What are deadlock, livelock, and starvation?

**Answer:** Deadlock is a cycle of threads waiting for locks. Livelock threads
keep reacting but make no progress. Starvation is indefinite lack of access to
a resource. Prevent deadlock with a global lock order, short ownership, time
bounds, and avoiding nested locks. Diagnose with a JVM thread dump and lock
ownership graph.

### Compare `Runnable`, `Callable`, `Future`, and `CompletableFuture`.

**Answer:** `Runnable` returns no value; `Callable<T>` returns `T` and can throw.
`Future<T>` represents a submitted result but blocking `get` is a boundary.
`CompletableFuture` composes stages: `thenApply` maps a value,
`thenCompose` flattens an async operation, `allOf` waits for several stages,
and `exceptionally` maps a failure. Always define executor ownership and
shutdown behavior.

```java
CompletableFuture<String> result =
    CompletableFuture.supplyAsync(() -> "catalog", executor)
        .thenCompose(name -> fetchPrice(name, executor))
        .exceptionally(error -> "unavailable");
```

### What is `ThreadLocal` and why can it leak?

**Answer:** Each thread has a map keyed by weak references to `ThreadLocal`
objects, but the values can remain strongly referenced by a long-lived pool
thread after the key is collected. Always call `remove()` in a `finally` block,
especially for request context, security identity, and tracing state.

### What are virtual threads and pinning?

**Answer:** A virtual thread is a JVM-managed lightweight thread scheduled over
carrier platform threads. It makes blocking I/O scale with many concurrent
tasks, but does not make CPU work faster. Blocking while holding certain
monitors or entering native code can pin a carrier, reducing scalability.
Measure before replacing every platform-thread design.

### What does a Java stream guarantee?

**Answer:** Intermediate operations are lazy; terminal operations execute the
pipeline. `map` keeps cardinality, `flatMap` expands/joins nested results, and
streams are single-use. `parallelStream()` adds scheduling, partitioning, and
combination overhead; it can be slower for small, stateful, ordered, or
blocking workloads.

### What are structured concurrency and `ScopedValue`?

**Answer:** Structured concurrency treats child tasks as a lexical unit whose
lifetime, cancellation, and failure are joined by the parent. `ScopedValue`
provides bounded, read-oriented context propagation without mutable
thread-local state. These are Java 21+ concepts; the repository's executable
course targets Java 17, so describe the newer API without silently mixing it
into the runnable source.

### What are functional interfaces and effectively-final capture?

**Answer:** `Predicate<T>` tests, `Function<T,R>` maps, `Consumer<T>` accepts,
and `Supplier<T>` creates values. A lambda can capture a local variable only
when it is final or effectively final, preventing ambiguous mutation across
execution timing/threads. Captured object state can still be mutable, so
effectively final does not mean deeply immutable.

### `orElse` versus `orElseGet` versus `orElseThrow`?

**Answer:** `orElse(value)` evaluates its value before the call, even when the
`Optional` is present. `orElseGet(supplier)` computes lazily only on empty.
`orElseThrow` converts absence into a chosen exception and is appropriate when
absence violates the method contract. Use an `Optional` at a boundary, not as a
field or a replacement for every null check.

## Code walkthrough

`ThreadsAndVisibility.java` intentionally contrasts a lost update with an
atomic counter. `ExecutorsAndFutures.java` demonstrates result, exception,
timeout, cancellation, and executor lifecycle. `FunctionalPipelines.java`
shows lazy streams and `Optional` evaluation.

Use `--release 17` for the course. Virtual-thread APIs, `StructuredTaskScope`,
and `ScopedValue` belong to newer JDKs; describe them as Java 21+ concepts and
do not silently add them to the Java 17 examples.

## Failure lab

Replace an atomic increment with `count++`, increase the worker count, and run
repeatedly. Explain why a single run can accidentally pass. Then remove
`executor.shutdown()` and observe that the process can stay alive: task
completion and resource lifecycle are different concerns.

## Revision card

```text
volatile gives visibility/order, not compound-operation atomicity.
CAS avoids blocking but needs a retry and ABA analysis.
Locks need an ownership order and bounded scope.
Executors own threads; callers own shutdown.
CompletableFuture composition must name the executor and error policy.
ThreadLocal state must be removed in pooled threads.
Parallel streams are a trade-off, not a default optimization.
```
