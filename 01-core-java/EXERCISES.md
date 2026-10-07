# Java practice lab and worked solutions

Run commands are in [README](README.md). Work on each exercise before opening its
solution. The supplied lessons are solved reference implementations; make your
practice edits locally and rerun the named class plus the aggregate runner.

## 1. Predict reference behavior

Inside `JvmAndValues.change`, replace `names.add("Grace")` with
`names = new ArrayList<>(names)`, then add `"Grace"` to that new list.
What does the caller observe? Can the method implement `swap(a, b)` for two
caller variables simply by reassigning parameters?

<details><summary>Worked answer</summary>

The caller still has `["Ada"]`: the new list receives the new element, while
the original is untouched. It remains pass-by-value in both versions. Parameter
reassignment cannot swap caller variables. Return a pair/result and let the
caller assign it, or explicitly mutate a caller-owned container. Test both
alias mutation and parameter reassignment rather than relying on terminology.
Update the expected result only for the deliberate altered exercise.

</details>

## 2. Design a generic transfer API

Generalize `copyIntegers` to transfer elements of any type from a producer to a
consumer. Explain why a `List<Number>` may receive integers but a `List<Double>`
may not. Add a test for empty input and a destination that already has values.

<details><summary>Worked solution</summary>

```java
static <T> void transfer(
        List<? extends T> source, List<? super T> destination) {
    destination.addAll(source);
}
```

For integer source and Number destination, inference can select an appropriate
T such as Integer. A Double destination is not a consumer of Integer. Verify
that `[0.5]` plus `[2,3]` becomes `[0.5,2,3]` and that empty input preserves the
destination. An unmodifiable destination throws: document mutation ownership
instead of silently copying. The signature does not promise atomic transfer,
deep cloning or safe concurrent access.

</details>

## 3. Repair a value key

A mutable `User` uses name in equals/hashCode; the name changes after insertion
into a HashMap. Why can `map.get(user)` fail? Design a safer identity model.

<details><summary>Worked answer</summary>

Insertion placed the key according to its old hash. The new hash can search a
different bucket even for the same object reference. Use an immutable UserId
as the key and keep editable profile data in the value. If business identity
really changes, explicitly remove under the old key and insert under the new
one, with appropriate synchronization/transaction semantics. The runnable
`CustomerId` checks demonstrate stable logical identity and equal hashes.
For a deliberate mutation experiment, a failed lookup is a possible broken
behavior, not something to depend on for every pair of old/new hashes.

</details>

## 4. Top-k and comparator contracts

You need the ten largest scores from a million values. Compare full sorting
with a size-ten min-heap. How would equal scores with different IDs be ordered?

<details><summary>Worked answer</summary>

Full sorting costs O(n log n) and retains/processes the full representation.
Maintain a min-heap of at most k elements: insert until full, then replace the
minimum only when the next value ranks higher. Processing is O(n log k),
auxiliary heap storage O(k); sorting the final k values costs O(k log k) if
ordered output is required. Define ties with an ID comparison; never subtract
integers in a comparator because subtraction can overflow. Use
`Comparator.comparingInt(...).thenComparing(...)`. Test k=0, fewer than k input
values, repeated scores, negative scores and boundary integers.

</details>

## 5. Draw the lost-update schedule

Replace the forced broken-counter schedule mentally with `counter++` on a
volatile field. Write an interleaving resulting in one instead of two. Would
sleeping before the final assertion make the counter correct?

<details><summary>Worked answer</summary>

`T1 read 0; T2 read 0; T1 write 1; T2 write 1`. Sleeping can wait for workers,
but cannot recover the lost update and does not replace a completion handoff.
Use `join` to await termination; use `AtomicInteger.incrementAndGet` or the
same synchronized monitor for the operation. For a balance-plus-audit invariant,
one atomic integer is insufficient: put the whole invariant under one
coordination protocol. The lesson uses latches to produce a reliable regression
case, not scheduling guesses.

</details>

## 6. Fix nested Future starvation

A single-thread executor runs task A. A submits B to the same executor and calls
`B.get()`. B would complete immediately if run. Explain the failure and fix.

<details><summary>Worked answer</summary>

A occupies the only worker while waiting; B is queued behind A. This is
thread-starvation deadlock, not necessarily a monitor cycle visible as a Java
lock deadlock. Increasing pool size may hide rather than solve the pattern.
Express dependency with `CompletableFuture.thenCompose`, so A completes its
stage without blocking a worker. Or perform B synchronously if it is just a
small local calculation. Bound waits at external boundaries and design
executor ownership independently. `get(timeout)` frees the waiter on timeout
but leaves B queued unless separately cancelled.

</details>

## 7. Extend transfer validation

Explain how `transfer` handles insufficient funds and destination overflow.
Add a negative-amount check and a self-transfer check to your local tests.
How would a concurrently queried “total balance” method acquire locks?

<details><summary>Worked answer</summary>

Insufficient funds are rejected before either mutation. `Math.addExact` computes
the destination result before debit, so an overflow cannot partially commit.
The supplied method already rejects negative amounts; assert source and
destination balances remain unchanged after that exception. A nonnegative
self-transfer returns unchanged. A snapshot of both balances must acquire
both locks in the same unique-ID order, copy both values, then release locks.
Independent locked reads of each balance are not a single consistent snapshot.
The sample is an in-memory teaching model, not a durable money-transfer service.

</details>

## 8. Preserve parse errors in a stream API

`parsePositive` collapses syntax errors and nonpositive values into empty.
Design a result that retains those causes. Should an unexpected database failure
also become empty? How do you avoid duplicate-key failures when counting?

<details><summary>Worked answer</summary>

Use a sealed result with success and explicit error variants, such as
`Parsed(int value)` and `Invalid(String input, Reason reason)`. Carry the result
through the pipeline or partition successes/errors intentionally. Do not turn
unexpected dependency outages into valid absence; propagate or translate at a
documented boundary. For counts, use `groupingBy(..., counting())` or
`toMap(key, ignored -> 1, Integer::sum)`. Test repeated keys, all-invalid input,
zero, whitespace policy and numeric overflow. Do not throw away errors merely
because a flatMap pipeline looks concise.

</details>

## 9. Records with mutable components

Would `record Payload(byte[] data)` be a safe immutable map key? Would
`List.copyOf` solve deep immutability if the list contained mutable Customer
objects instead of Strings?

<details><summary>Worked answer</summary>

Not automatically. The array is mutable through constructor/accessor aliases,
and generated record equality compares array references, not contents.
An immutable byte-value type needs copies on input/output and deliberate
content equals/hashCode (or an appropriate immutable representation).
Copying a list only isolates its structure; mutable Customer elements remain
shared. Snapshot each relevant element into an immutable value object if deep
immutability is required. Add mutation-after-construction tests and logically
equal-but-separately-allocated payload tests.

</details>

## 10. Diagnose a two-sum regression

A candidate inserts the current value into `seen` before lookup, and computes
the complement as `int required = target - value`. Give two minimal failing
cases and explain both fixes.

<details><summary>Worked answer</summary>

- `[3]`, target `6` falsely succeeds because insertion permits reusing one
  index. Check before insertion.
- `[Integer.MAX_VALUE, 1]`, target `Integer.MIN_VALUE` can falsely succeed
  because int arithmetic wraps. Calculate `(long) target - value` and store
  compatible widened values; the slow oracle must also widen before addition.

The supplied implementation and targeted tests cover both. If oracle and
optimized implementation both have the same overflow bug, differential
agreement alone is misleading—that is why explicit mathematical boundary
expectations accompany the exhaustive small-domain checks.

</details>

## Final mixed interview (45–60 minutes)

**Prompt:** Design an in-memory reservation API that accepts order IDs and item
quantities, rejects invalid input, never oversells under concurrency, supports
duplicate request detection, and returns explicit results.

Spend 5 minutes defining semantics, 20 minutes coding a small version, 10 minutes
testing, and 10 minutes discussing limitations. Don't introduce a framework.

**Worked design outline:**

1. Immutable ID value and validated positive quantity (widen/check arithmetic).
2. Explicit result variants for reserved, duplicate and unavailable. Decide
   whether duplicate returns the original success or an error.
3. One monitor for a first small correct model: under it, check prior request,
   check stock, decrement and store outcome. A ConcurrentHashMap plus a separate
   atomic stock counter does **not** make the combined operation transactional.
4. Return immutable snapshots rather than exposing mutable internal maps.
5. Tests: zero/negative input, exact remaining stock, oversubscription, repeated
   request, duplicate request with changed payload, and many concurrent unique
   requests. Use latches/futures, bounded waits and propagated task errors.
6. Discuss memory growth of deduplication state, expiry semantics, fairness,
   persistence, process restarts and multi-instance coordination. State plainly
   that an in-memory monitor cannot coordinate separate JVMs.

**Self-assessment:** Can you state the invariant, identify its linearization
point, explain every happens-before edge, demonstrate failure-path atomicity,
and name the first production limitation? An API that merely “uses synchronized”
without explaining these details is not a complete answer.
