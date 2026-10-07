# Core Java: concepts, interview answers and tradeoffs

Use this alongside the runnable files in the [lesson table](README.md).
An interview answer should state a contract, illustrate it, name a limitation,
and explain when it affects an engineering choice.

## 1. JVM execution, memory and pass-by-value

**Run:** `java -cp out com.interviewprep.core.JvmAndValues`

Java source compiles to bytecode. A JVM loads classes, verifies and links them,
then initializes static state when required by an active use. An interpreter
and JIT compiler can execute the same method at different stages; hot code may
be optimized, deoptimized and recompiled. “Java is interpreted” and “Java is
compiled” are both incomplete explanations.

### A useful memory model

- Each thread has a stack of frames holding locals, operand state and return
  information. Primitive locals and reference values may conceptually be in
  frames; optimization can keep values in registers or eliminate them.
- Objects and arrays are conceptually allocated in the managed heap. References
  can live in frames **or in other objects**. Escape analysis/scalar replacement
  may remove allocations, so “every object physically sits on the heap” is too
  literal as an implementation claim.
- Class metadata lives in JVM-managed native memory (HotSpot metaspace), not
  simply in the Java heap. Code cache, thread stacks, direct buffers and native
  libraries also consume memory. `-Xmx` is not a total process-memory cap.
- GC begins from roots such as live thread state and static references in live
  classes, follows references and reclaims eligible unreachable objects. Cycles
  do not inherently leak. A static cache retaining unused entries can leak in
  an application sense because those entries are still reachable.

Young/old generations exploit the common observation that many objects die
young, but details vary by collector and JVM version. G1, commonly the server
default in HotSpot 17, manages regions and targets pause goals; those goals are
not hard latency guarantees. Parallel GC favors throughput. ZGC emphasizes
short pauses at other resource costs; do not assume newer generational ZGC
features exist in Java 17. Select based on measured workload and deployment,
not a universal “best collector.”

**Q: Is Java pass-by-reference?** No: it is always pass-by-value. Passing an
object copies a reference value. Two copies can point to the same object, so
mutation is visible, but assigning the parameter to a new object does not
change the caller's variable. The lesson's list gains `"Grace"` but never
becomes the newly allocated list. Passing `7` cannot change the caller to `99`.

**Q: Does loading initialize a class?** Not necessarily. The lesson uses
`Class.forName(name, false, loader)` to load without requested initialization,
then reads a non-constant static field to cause active initialization. Reading
a compile-time constant may be inlined and need not initialize its declaring
class. Initialization is synchronized by the JVM and occurs once for a given
class identity, not once per every textual class name in the process.

**Q: What makes two classes the same type?** Their binary name **and defining
class loader**. In plugin systems, identically named classes from different
loaders can be incompatible. Parent delegation helps share platform classes.
A loader or its classes retained by threads/caches can prevent unloading.

**Pitfalls:** `System.gc()` is only a request; finalization is not reliable
resource cleanup; setting one variable to null does not remove other references.
Use try-with-resources for owned files/sockets. An allocation failure can involve
heap, native threads or metaspace, so investigate the actual error and memory
region rather than immediately increasing `-Xmx`.

## 2. Primitives, generics and exception boundaries

**Run:** `java -cp out com.interviewprep.core.TypesGenericsExceptions`

`int` is signed 32-bit; `long` is signed 64-bit. Ordinary integer overflow wraps.
`5 / 2` computes `2`, and assigning it to `double` afterwards yields `2.0`.
Promote an operand *before* arithmetic: `5 / 2.0`. Similarly `(long) a + b`
widens before addition, whereas `(long) (a + b)` can preserve an already
overflowed result. Use `Math.addExact` when overflow must reject an operation.
Money often uses integer minor units or `BigDecimal` with an explicit scale and
rounding rule, not binary floating point.

Boxing converts a primitive to a wrapper object/value representation;
unboxing `null` throws. Wrapper `==` compares reference identity, except when
unboxing is triggered by the expression. Small cached `Integer` instances can
make bad identity-based tests accidentally pass. Use logical equality
(`Objects.equals`) when null is possible. A Java `char` is a UTF-16 code unit,
not necessarily a whole Unicode character.

**Q: Why isn't `List<Integer>` a `List<Number>`?** If that assignment were legal,
code could insert `Double` into an integer list. Generics are invariant.
`List<? extends Number>` is a producer of `Number`; the unknown subtype prevents
inserting a specific non-null Number. `List<? super Integer>` consumes Integer,
but reading promises only Object. PECS means “producer extends, consumer super.”
It describes how a parameter is used, not a universal rule that every API
parameter needs a wildcard.

**Q: What survives erasure?** Runtime objects typically do not carry distinct
`ArrayList<Integer>` versus `ArrayList<String>` classes. Compiler-generated
casts/bridges preserve the static model; some declaration-level generic
metadata is available to reflection. You cannot test `instanceof List<String>`
or create `new T[]` normally. Arrays are reified and covariant, so an invalid
store may throw `ArrayStoreException` instead of being rejected at compilation.

Checked exceptions must be caught or declared; unchecked exceptions include
`RuntimeException` subclasses. The choice should model recoverable API
boundaries, not “checked is always good/bad.” Catch specific failures, preserve
causes, and don't transform infrastructure errors into apparently valid data.
`Error` generally signals conditions application code should not broadly
recover from. The small check helper catches `Throwable` only to report test
failures, not as a model for production error handling.

**Q: What if both work and cleanup throw?** Try-with-resources retains the body
failure as primary and attaches closing failures as *suppressed* exceptions.
Resources close in reverse acquisition order. A return/throw in `finally`
can hide the real failure; avoid it. The executable verifies that cleanup
actually ran and its failure was not discarded.

## 3. OOP as contracts, not just syntax

**Run:** `java -cp out com.interviewprep.core.ObjectDesign`

Encapsulation keeps representation private and preserves invariants through
operations. Abstraction exposes what clients need instead of implementation
details. Polymorphism lets clients invoke a contract implemented by different
behaviors. Inheritance creates an is-a relationship and couples a subclass to
base behavior; composition builds behavior from collaborators.

In the example, `Checkout` owns a `Discount` interface. Pricing rules can vary
without subclassing checkout. Inputs are nonnegative minor units; outputs must
stay between zero and the original amount. Injecting an invalid strategy is
detected instead of silently charging more. This illustrates Liskov substitution:
a subtype must preserve behavioral promises, not merely method signatures.

**Q: Explain SOLID with this example.**

- **Single responsibility:** checkout orchestrates pricing; a strategy owns a rule.
- **Open/closed:** add a strategy without rewriting every checkout branch.
- **Liskov substitution:** all strategies respect the documented price contract.
- **Interface segregation:** clients depend on a small discount operation.
- **Dependency inversion:** orchestration accepts an abstraction instead of
  constructing one hard-coded discount.

These are design heuristics. Creating dozens of one-method abstractions for a
stable local calculation may make the code harder to follow, not better.
An abstract class is useful for shared state/template logic; an interface
defines a role across unrelated implementations and can supply default methods.
Default-method conflicts still need an explicit resolution.

**Q: What is the equals/hashCode contract?** Equality is reflexive, symmetric,
transitive, consistent while relevant state is unchanged, and false for null.
Equal objects must have equal hashes; unequal objects **may** collide.
Hash collections first select candidate buckets, then use equality.
The final immutable `CustomerId` keeps identity stable and avoids subclass
equality asymmetry.

**Pitfall:** Mutating a key's equality/hash fields after insertion can strand
it in the wrong bucket. Merely overriding `hashCode` is not enough, nor is
overriding only `equals`. Avoid inheriting value equality into subclasses that
add identity fields. Prefer final value objects/records or an explicitly
designed hierarchy. The cents example floors the discount; rounding is a
business policy that tests must capture.

## 4. Collections: operation costs and surprising contracts

**Run:** `java -cp out com.interviewprep.core.CollectionChoices`

| Structure | Common costs | Caveat / good use |
|---|---|---|
| `ArrayList` | get O(1), append amortized O(1), middle insert/remove O(n) | Resizing copies references; strong locality |
| `LinkedList` | index access O(n), end operations O(1) | Insertion O(1) only when position/node iterator is already known; allocation overhead |
| `ArrayDeque` | end operations amortized O(1), search O(n) | No null; prefer to legacy `Stack` for stack/queue use |
| `HashMap`/`HashSet` | expected O(1) lookup/update | Hash quality, resizing, collisions and key costs matter |
| `TreeMap`/`TreeSet` | O(log n) operations | Comparator establishes order and equality for membership |
| `PriorityQueue` | peek O(1), insert/poll O(log n), arbitrary search O(n) | Iteration is not sorted order |
| `CopyOnWriteArrayList` | read O(1), write O(n) copy | Snapshot iterators; appropriate only for read-heavy, small-ish sets |

Costs assume O(1) element comparison/hash unless stated. Long string hashes and
expensive comparators add their own cost. HashMap's tree bins in modern HotSpot
libraries mitigate certain collision patterns, but do not advertise an
unconditional worst-case O(log n) guarantee for all adversarial keys and
comparison behaviors. Iterating HashMap involves capacity as well as size.
Map iteration order is not insertion order unless a specific implementation
such as `LinkedHashMap` promises it.

**Q: Why did TreeSet drop `"Bob"` after `"Ada"`?** The comparator only compared
lengths, returning zero. A sorted set treats comparison zero as equivalent for
membership, even though `String.equals` says they differ. Add a lexical
tie-breaker if distinct equal-length strings must be retained.

**Q: Is an unmodifiable collection immutable?** Its own mutation API is blocked,
but referenced elements may remain mutable. `Collections.unmodifiableList`
wraps a live backing collection; `List.copyOf` snapshots element references.
`subList` is a view; non-structural changes are shared. Structural modifications
of the parent outside that view can invalidate it. `Arrays.asList` is a fixed-size
array-backed view, not the same as either example.

Fail-fast iterators detect some concurrent structural modification bugs on a
best-effort basis, not as a synchronization guarantee. Use the iterator's
supported remove operation or `removeIf` for controlled removal. Use concurrent
collections when required; never build correctness around catching
`ConcurrentModificationException`.

## 5. Threads, the memory model, visibility and atomicity

**Run:** `java -cp out com.interviewprep.core.ThreadsAndVisibility`

A `Thread` is an execution mechanism; `Runnable` is work with no result.
`start()` schedules a new thread which invokes `run()`. Calling `run()` yourself
is a normal call on the current thread. Thread scheduling is nondeterministic;
neither priority nor a short sleep provides a correctness guarantee.

The Java Memory Model describes which writes another thread is guaranteed to
observe. Useful happens-before edges include:

1. Actions before `Thread.start()` to actions in the started thread.
2. Worker actions to a successful `join()` return.
3. Monitor unlock to subsequent lock of the same monitor.
4. Volatile write to subsequent read of that variable.
5. Documented handoffs such as latch release/await and task completion/Future.get.

**Q: Why doesn't `volatile int count; count++` work?** Volatile provides visibility
and ordering for individual reads/writes. Increment consists of read, addition
and write. Both workers can read zero and write one. The lesson forces exactly
that schedule with latches instead of hoping a stress loop reproduces it.
`AtomicInteger.incrementAndGet` provides a single atomic read-modify-write.

**Q: Can volatile safely publish a result?** Writing ordinary result fields,
then a volatile `ready = true`, can make those preceding writes visible to a
reader that observes `ready`. This is useful for one-way publication. It does
not automatically protect subsequent mutation or preserve a multi-field
invariant. An immutable result handed off through a Future is often clearer.
Safe construction matters: don't let `this` escape before construction ends.

**Q: What should interrupted code do?** Interruption is a cooperative request,
not forced termination. Propagate `InterruptedException` when possible.
Otherwise restore the flag with `Thread.currentThread().interrupt()` after
necessary cleanup. Avoid swallowing it and continuing indefinitely.

`sleep` keeps any locks already held; `wait` releases the monitor being waited
on and must be called while owning it. Wait in a loop that rechecks the
predicate because wakes can be spurious or another consumer can consume the
condition. Prefer higher-level synchronizers/queues rather than writing a
custom monitor protocol in application code.

## 6. Executors, futures and resource ownership

**Run:** `java -cp out com.interviewprep.core.ExecutorsAndFutures`

Executors separate task submission from scheduling. A `Callable<T>` produces a
result or throws; `Future<T>` exposes completion, cancellation and blocking
retrieval. Use a bounded wait at application/test boundaries. `get()` publishes
task results and wraps task failures in `ExecutionException`. Ignoring a
submitted Future can hide failures.

**Q: Did a timed-out Future stop running?** No. `get(timeout)` only limits how
long the caller waits. `cancel(true)` attempts to interrupt a running task or
prevent a queued task from starting. A future marked cancelled does not prove
the underlying code has exited. Blocking APIs and task loops must cooperate.
The lesson blocks on an unreleased latch, times out, then requests cancellation;
it does not depend on an arbitrary worker sleep.

`CompletableFuture` allows nonblocking stage composition. `thenApply` transforms
a value; `thenCompose` flattens an async result. `thenCombine` combines independent
results; `exceptionally` recovers to a value; `handle` sees success or failure.
`join()` uses unchecked completion exceptions, unlike checked exceptions from
`get()`. Non-async callbacks may execute on the completion thread (or attaching
thread if already complete). Async stages without an executor usually use the
common pool; be deliberate about where blocking or expensive work runs.

**Q: Is a fixed thread pool bounded?** Worker count is bounded, but
`Executors.newFixedThreadPool` uses an unbounded work queue. Sustained overload
can exhaust memory. A production `ThreadPoolExecutor` may need a bounded queue,
rejection/backpressure policy and metrics. The sample submits only a handful of
tasks and does not claim production capacity management.

**Q: Why not try-with-resources around this executor?** That API is not available
on Java 17. The owner must call shutdown, await termination and potentially
shutdownNow. ShutdownNow is still interruption, not a kill switch.
Blocking inside a task waiting for another task in the same saturated pool
can cause thread-starvation deadlock; compose stages instead.

## 7. Locks, deadlock prevention and ConcurrentHashMap

**Run:** `java -cp out com.interviewprep.core.LocksAndConcurrentMaps`

Synchronization protects a **compound invariant**. Two thread-safe fields do
not make transferring money between accounts atomic. All code that accesses a
shared invariant must use the same locking policy. The sample locks both
accounts before validating and mutating; overflow is computed before debiting
so an exception leaves the balances unchanged.

Intrinsic `synchronized` monitors are reentrant and release on scope exit,
including exceptions. `ReentrantLock` supports additional features such as
interruptible acquisition, timed tryLock, multiple conditions and optional
fairness. Unlock in `finally`; fairness can reduce throughput and does not
promise a universal operating-system scheduling order.

**Q: How would opposite transfers deadlock?** Thread A could hold account 1 and
wait for account 2 while B holds 2 and waits for 1. Deadlock requires conditions
including circular wait. The example always locks the lower unique account ID
first, preventing that cycle. IDs must truly establish a total order; equal IDs
on distinct accounts are rejected. Do not run a deliberately deadlocking test
that leaves non-daemon threads hanging.

**Q: Does ConcurrentHashMap make get-plus-put atomic?** No. Each operation is
thread-safe, but another thread can update between them. Use `merge`, `compute`
or `putIfAbsent` for the appropriate per-key operation. The example counts
4,000 transfers with atomic `merge`. Mapping/remapping functions should be short,
avoid external side effects, and not perform recursive updates to the map.

ConcurrentHashMap permits no null keys/values, giving unambiguous absence
semantics. Iterators are weakly consistent, not frozen snapshots. Concurrent
aggregate observations such as `size()` are not transactionally coordinated
with writers. Per-key atomicity does not cover two accounts or two keys.
For very hot approximate live metrics, `LongAdder` can reduce contention;
its sum is not an atomic snapshot under concurrent updates. After writers
finish, exact totals can be checked.

**Pitfalls:** Holding locks around I/O increases latency and deadlock risk.
`tryLock` plus retries can still livelock without a policy. Multiple locks need
an audited acquisition order. The sample reads account fields only after all
Futures complete; a real service needs a consistently locked snapshot method.

## 8. Lambdas, streams and Optional

**Run:** `java -cp out com.interviewprep.core.FunctionalPipelines`

A lambda supplies an implementation of a functional interface with one abstract
method. Captured local variables must be final or effectively final; captured
objects can still be mutable. That restriction alone does not make a lambda
pure or thread-safe.

Streams describe a pipeline. Intermediate operations are generally lazy;
terminal operations trigger traversal. Map transforms one element to another;
flatMap emits zero or more elements. Filter retains matching elements.
The example parses tokens into Optionals, flattens only successful positive
values, and then groups/counts them.

**Q: Why use mapToInt?** Primitive streams avoid some boxing overhead and supply
numeric operations. It doesn't mean a stream always beats a loop: allocation,
pipeline overhead, input size, short-circuiting and JIT behavior matter.
Stateful operations such as sorted/distinct can require buffering. Stream
sorted generally needs O(n log n) work; a min operation only needs O(n).

**Q: What does a correct reduction require?** For parallel reduction the
combining operation must be associative and the identity must be neutral.
Subtraction is not associative. Floating-point addition is not perfectly
associative either, so regrouping can affect numeric results. Mutating one
shared accumulator from parallel forEach is a race; use a suitable collector
or an explicit safe algorithm.

**Q: Why did Optional.orElse call an expensive fallback?** Java evaluates method
arguments eagerly. `orElse(expensive())` evaluates it even for a present value.
`orElseGet(() -> expensive())` defers evaluation. Optional models absence, not a
typed failure reason. The parser intentionally collapses “invalid integer” and
“not positive”; use a richer result if those causes matter.

Avoid unchecked `get`, returning null instead of Optional.empty, and treating
Optional as a serialization/entity-field requirement. `Optional.of(null)`
throws; `ofNullable` models a nullable boundary. `map` wraps a non-null result,
`flatMap` combines an Optional-returning function without nesting.

**Pitfalls:** Streams are single-use. `Stream.toList()` (available since 16)
returns an unmodifiable list, not a promise of a particular concrete class.
`Collectors.toList()` makes no general mutability guarantee. Duplicate keys
need an explicit merge policy with `toMap`. Side effects in peek/map may be
skipped by optimizations or execute unexpectedly in parallel; do not use them
as mandatory business actions. Parallel streams are a choice to benchmark, not
an automatic acceleration flag.

## 9. Java 17 records, sealed types and finalized syntax

**Run:** `java -cp out com.interviewprep.core.ModernJava17`

Records supply component accessors, canonical construction, equality, hashCode
and toString. Their component fields are final and the record class is final.
They can implement interfaces, but cannot extend an arbitrary class.
A compact constructor validates/transforms parameters before implicit field
assignment. The example uses `List.copyOf` to isolate a list supplied by a caller.

**Q: Are records deeply immutable?** No. A final reference can still point at a
mutable list or array. Defensive copies and safe accessor design are necessary.
The supplied order contains immutable String elements, so the list copy is
enough for that model. An array component would have both aliasing concerns
and reference-based generated equality unless you designed around them.

A sealed interface/class restricts permitted direct subtypes. A subtype must
be final, sealed or non-sealed; records are already final. In an unnamed module
permitted types must share a package; named modules allow the appropriate
same-module relationship. Sealing closes a variant set for modeling results,
commands or domain events; it is not a security mechanism.

**Q: Which pattern features are final in 17?** `instanceof` pattern binding is
final; records and sealed classes are final; switch expressions are final.
Pattern matching for switch is a preview feature in 17; record patterns belong
to later Java releases. This course uses ordinary `instanceof` branches, so
adding a variant does not give compile-time exhaustiveness checking for that
if chain. A record is a data carrier, not automatically a good persistence
entity or a replacement for every stateful object.

`var` is local static type inference, not dynamic typing. Text blocks improve
multiline string readability but do not sanitize SQL/HTML or interpolate
variables automatically.

## 10. Testing, debugging and performance reasoning

**Run:** `java -cp out com.interviewprep.core.TestingAndPerformance`

The exercise is “do two distinct indices sum to this target?” The simple
reference tests all pairs: O(n²) time, O(1) auxiliary space. The optimized
version tracks seen values in a hash set: expected O(n) time, O(n) space.
It checks before inserting to avoid using one element twice. Arithmetic is
widened before calculating complements, avoiding overflow false positives.

**Q: How would you test an optimized algorithm?** Use:

1. Examples with known expected outcomes, including empty and singleton inputs.
2. Boundaries: duplicates, negative numbers, min/max integers and overflow.
3. A simple independent oracle for small cases.
4. Deterministic exhaustive enumeration of a bounded domain, or seeded random
   property checks when enumeration is too large.
5. A property such as agreement with the oracle—not merely “didn't throw.”

The lesson covers all arrays of length 0–5 with elements -2 through 2 and
targets -5 through 5: `(1 + 5 + 25 + 125 + 625 + 3125) * 11 = 42,966`.
This is broad within that finite domain, not a proof for arbitrary arrays.
Separate assertions cover integer extremes outside the enumerated domain.

### A practical debugging workflow

1. Reproduce on the smallest fixed input and record expected versus actual.
2. Read the full exception chain, including causes and suppressed exceptions.
3. Put a breakpoint immediately before the invariant first becomes false.
   For two-sum, inspect `required` and `seen`, not just the eventual return.
4. Test a specific hypothesis: did insertion happen before lookup? Did addition
   overflow before conversion? Do both threads read the same old value?
5. Add a regression case, apply the fix, rerun targeted checks.

For running JVMs you own, `jcmd <pid> Thread.print` can reveal blocked threads
and deadlocks; `jcmd <pid> GC.heap_info` gives a starting point for memory
investigation. Discover the relevant process ID rather than attaching to
unrelated services. Heap dumps and recordings can contain sensitive data;
collect and retain them deliberately. Thread dumps alone do not prove the
absence of a race. JFR can help connect CPU, allocation and blocking evidence.

**Q: Is expected O(n) always faster than O(n²)?** No. On tiny arrays a simple
contiguous double loop may beat hashing, boxing and allocation. Complexity
describes scaling, not every measured point. Hashing buys asymptotic work by
using memory; sorting plus two pointers is another option with O(n log n)
time and representation/mutation tradeoffs.

Do not benchmark a single method call with nanoTime and publish a general
conclusion. Warm-up, JIT compilation, dead-code elimination, constant folding,
GC, machine load and input distribution matter. A real benchmark should use a
dedicated harness such as JMH with forks, warm-up and consumed results; no such
harness or benchmark is included here. First establish correctness, profile a
representative workload, and measure throughput **and** tail latency.
