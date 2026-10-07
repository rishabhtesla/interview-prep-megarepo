# Interview playbook

## The answer pattern: claim, mechanism, example, boundary

A strong technical answer does more than name the right feature.

**Question:** Is `volatile` enough for a shared counter?

**Weak answer:** "`volatile` makes variables thread-safe."

**Strong answer:** "`volatile` provides visibility and ordering guarantees for accesses
to that field, but `count++` is a read-modify-write sequence. Two threads can both
read 4 and both write 5. `AtomicInteger.incrementAndGet()` makes that increment
atomic; a lock is appropriate when multiple fields must change under one invariant.
A concurrent counter still does not automatically make the surrounding workflow
atomic."

Use the same four-part structure for React hooks, indexes, Spark joins, and transactions.
Be precise about guarantees, not just common behavior.

## Coding interview: a 45-minute script

### Minutes 0-5: clarify

Ask about input size, value ranges, duplicates, ordering, mutability, invalid input,
output format, and whether multiple answers are valid. Do not ask every possible
question mechanically; ask the ones that change the solution.

For Two Sum, important questions include whether exactly one answer exists and whether
the same index can be reused. Alphabet size matters for character-frequency problems.
Recursion depth matters for a degenerate tree.

### Minutes 5-10: derive

Describe the obvious solution and its cost. Then identify repeated work.
For Two Sum, scanning all pairs repeats searches for a complement. A map remembers
previous values. The invariant is: before iteration `i`, the map contains only
indices less than `i`. Looking up before inserting prevents reusing the current index.

This is a proof sketch, not a performance slogan.

### Minutes 10-30: implement

Use clear names and narrow helpers. Narrate decisions, not keystrokes. If stuck,
say which assumption failed and return to the invariant. Do not fill the remaining
time with speculative code.

When requirements allow only lowercase English letters, a 26-element frequency array
is sensible. If the interviewer expands the domain to Unicode text, discuss code
points, normalization, and the chosen definition of a character before promising
that changing the array to a map solves everything.

### Minutes 30-40: test deliberately

Choose cases by failure mode:

| Failure mode | Candidate case |
| --- | --- |
| Off-by-one | Zero, one, or two elements |
| Duplicate handling | All equal values |
| Ordering assumption | Descending or already sorted data |
| Invalid reuse | One value whose double equals the target |
| Missing answer | Valid input with no feasible result if the contract permits it |
| Numeric overflow | Near-boundary integer values if allowed by constraints |
| Recursion growth | A chain rather than a balanced tree |
| Aliasing/mutation | Caller reuses the input after the call |

Do a short dry run with the actual variable values. "It handles edge cases" is
not evidence.

### Minutes 40-45: discuss extensions

State average vs worst-case complexity, auxiliary vs output space, and any mutation.
If asked to scale, first identify what no longer fits: time, memory, network,
latency, or the consistency requirement.

## Debugging interview: a repeatable method

1. Write the expected and observed behavior precisely.
2. Minimize the reproduction while preserving the failure.
3. Form one falsifiable hypothesis.
4. Inspect the relevant state, plan, log, or breakpoint.
5. Fix the cause and add a regression case.
6. Explain adjacent risks without rewriting unrelated code.

### Probe A: map lookup fails

**Scenario:** You insert a user object into a `HashMap` and later cannot find it.

**Questions:** Which fields participate in `equals`/`hashCode`? Did any change after
insertion? Is the lookup using the same equality contract? Is the key's hash stable?

**Worked explanation:** A hash-based map uses the hash to choose a bucket. Mutating
a field used in the hash can make the lookup inspect a different bucket from the
one used at insertion. Use an immutable key or a stable identifier, not a mutable
domain object with changing equality. Increasing map capacity is unrelated.

### Probe B: React displays old results

**Scenario:** Search for "java", quickly search for "scala", and see Java results.

**Worked explanation:** The first request may finish last. Effects and promises do
not guarantee network completion order. Associate responses with the active request
or cancel stale work using an `AbortController`, and handle abort distinctly from
real failures. React Strict Mode can reveal missing cleanup during development; it
is not a reason to disable Strict Mode instead of fixing the effect.

### Probe C: one Spark task takes much longer

**Scenario:** Most tasks finish in seconds; one takes twenty minutes after a join.

**Worked explanation:** Inspect partition sizes, shuffle read, spill, and executor
logs. A hot key can concentrate work in one partition. More executors do not split
one indivisible hot-key task. Possible remedies depend on semantics: filter earlier,
pre-aggregate, broadcast a truly small side, use AQE skew handling when applicable,
or salt and recombine an associative aggregation. Salting is not universally valid.

### Probe D: a retry creates duplicate orders

**Scenario:** The client times out after a database commit, retries, and gets a
second order.

**Worked explanation:** A timeout is ambiguous: it does not prove the server did
nothing. Use a client-provided idempotency key scoped to the caller and operation,
with a database uniqueness constraint and stored request fingerprint/result.
Reject reuse with a different payload. Handle concurrent initial requests atomically.
A retry counter and a cache without durable uniqueness do not close the race.

## System design interview: a 50-minute script

| Time | Activity | Deliverable |
| --- | --- | --- |
| 0-7 | Requirements | Actors, core operations, invariants, exclusions |
| 7-12 | Scale and service goals | Requests/sec, storage, latency/freshness targets, assumptions |
| 12-20 | Model and APIs | Keys, relationships, operations, ownership |
| 20-30 | Architecture | Read path, write path, consistency and failure boundaries |
| 30-42 | Deep dive | One difficult issue: skew, transactions, deduplication, ordering, or cache invalidation |
| 42-50 | Operability and recap | Failure modes, metrics, security, trade-offs, next bottleneck |

Do not draw ten boxes before clarifying whether overselling inventory is acceptable.
The invariant determines architecture. See [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md)
for worked cases.

## Scoring rubric

Score each dimension 0-4. These are practice metrics, not an employer's hiring rubric.

| Score | Meaning |
| --- | --- |
| 0 | No relevant progress or a fundamental misconception |
| 1 | Progress only after substantial direction |
| 2 | Mostly correct with gaps in boundaries, explanation, or completion |
| 3 | Independently correct and clearly explained |
| 4 | Correct, concise, robust to follow-ups, and explicit about trade-offs |

| Dimension | Coding evidence | Design evidence |
| --- | --- | --- |
| Clarification | Constraints influence algorithm choice | Requirements influence consistency and boundaries |
| Reasoning | Invariant and complexity are defensible | Assumptions and capacity estimates are consistent |
| Correctness | Typical and adversarial inputs work | State transitions preserve business invariants |
| Communication | Explains why and responds to hints | Explains ownership, failures, and alternatives |
| Adaptability | Adjusts when constraints change | Identifies the next bottleneck without overengineering |

Do not hide a zero in correctness behind a high average. Repeat a comparable prompt
until the critical misconception is repaired.

## Mixed mock loops

The prompts below are original practice exercises, not descriptions of any company's
actual confidential interview.

### Loop 1: Java backend

**Coding, 45 minutes:** Return the length of the longest substring without repeated
characters. Start with an explicit alphabet contract. Follow up with returning
the substring and handling ties.

**Concepts, 20 minutes:** Compare a `HashMap`, `ConcurrentHashMap`, and a synchronized
map. Explain whether a thread-safe collection makes a check-then-act operation safe.

**Design, 45 minutes:** Design checkout without overselling. Discuss idempotency,
payment uncertainty, reservations, compensation, and reconciliation.

**Project, 15 minutes:** Describe a reliability improvement you personally made.

**Evaluator notes:** A sliding window needs to move its left boundary forward,
never backward. Atomic map methods solve specific races, not arbitrary cross-key
invariants. Checkout should acknowledge the lack of one transaction across payment
and inventory.

### Loop 2: data engineering

**Coding, 45 minutes:** Find the top `k` most frequent identifiers. Discuss heap,
sorting, and bucket alternatives and when `k` approaches the number of distinct keys.

**SQL, 25 minutes:** Compute each user's latest activity and seven-calendar-day
activity count with duplicate timestamps handled deterministically.

**Spark, 30 minutes:** A join multiplies totals and one stage spills. Diagnose
cardinality and skew separately; do not assume one explains the other.

**Design, 45 minutes:** Design retry-safe daily revenue aggregation with late events
and corrections.

**Evaluator notes:** Distinguish event time from ingestion time, duplicate records
from legitimate repeated business events, and additive metrics from non-additive
ones. Explain how a corrected day replaces or adjusts published output.

### Loop 3: full-stack

**Coding, 45 minutes:** Merge overlapping intervals. Clarify whether touching
intervals overlap and whether the input may be mutated.

**React, 30 minutes:** Design a filtered catalog with asynchronous loading, an
accessible progress update, and error recovery. Explain stale state and stale requests.

**API, 25 minutes:** Define completion update semantics, including repeated requests,
unknown items, validation, and concurrent updates.

**Design, 45 minutes:** Extend the study application to multiple users and devices.

**Evaluator notes:** A UI cannot enforce authorization on its own. Optimistic UI
must handle failure or conflict explicitly. Persistent last-write-wins behavior is
a product decision, not the same as conflict detection.

## Concept follow-up bank

Try each before reading its track's explanation.

| Prompt | What a strong answer must include |
| --- | --- |
| Why can a Java memory leak exist with GC? | Objects remain reachable from roots; garbage collection does not infer business usefulness |
| Is `ArrayList` insertion O(1)? | Amortized append, resizing cost, indexed shifting, and position matter |
| Why not parallelize every stream? | Work size, splitting, shared state, pool contention, ordering, and blocking |
| Is a Scala `Future` lazy? | Generally starts when constructed with an execution context; transformations do not turn it into a lazy effect |
| When is `fold` safer than `reduce`? | An explicit identity supports empty input; associativity matters for parallel aggregation |
| Does `cache()` immediately load a Spark dataset? | Persistence is requested; computation/materialization occurs through actions |
| Is a broadcast join always faster? | Driver/executor memory, actual size, broadcast cost, and join semantics matter |
| Does `@Transactional` cover an HTTP call? | Only configured transactional resources participate; proxy boundary and transaction manager matter |
| Can an index make writes slower? | Index maintenance, storage, contention, and selectivity/read benefit trade-offs |
| Does `useEffect(..., [])` mean once globally? | Component lifecycle, remounts, and development Strict Mode; not a global singleton |
| Is `useMemo` a semantic guarantee? | Performance optimization; correctness must not depend on cached value retention |
| Can retries improve every failure? | Permanent vs transient errors, idempotency, deadlines, backoff, jitter, and retry storms |

## Last-day preparation

Revisit your error log, run one moderate coding exercise, rehearse two truthful
project stories, and confirm the interview environment. Do not learn an entirely
new framework the night before. Prepare questions about the team's actual
engineering work, ownership, feedback, and reliability practices.
