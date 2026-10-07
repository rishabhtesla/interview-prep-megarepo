# Scala practice lab with worked answers

Use the [README commands](README.md) and compile the entire source directory
so the shared pinned configuration and checks are included. These exercises
extend the supplied solved lessons; try your own implementation first.

## 1. Evaluation and stack safety

Predict the number of calls to an effectful `load()` in each expression:

1. `def twice(x: Int) = x + x; twice(load())`
2. `def twice(x: => Int) = x + x; twice(load())`
3. The by-name version with `lazy val saved = load(); twice(saved)`

Then explain why a naive `n * factorial(n - 1)` is not tail recursive.

<details><summary>Worked answer</summary>

The counts are one, two, and one respectively, assuming both operands are
evaluated normally and calls succeed. A normal parameter evaluates before
entering the method; by-name reruns its expression on each access; lazy val
memoizes the successful computation.

In naive factorial, multiplication remains after the recursive return. Move
the product into an accumulator so the recursive call is the final operation,
then annotate the helper `@tailrec`. The supplied BigInt factorial checks zero
and five; add a negative-input rejection and a recurrence property for a small
range. BigInt avoids overflow but its size grows, so don't claim constant total
memory just because the recursion is stack-safe.

</details>

## 2. Extend the Candidate domain

Add a department to Candidate and write a function that returns the name of the
first candidate meeting a threshold. Handle empty input without `.head` or
`.get`. What does copying a candidate mean if department is mutable?

<details><summary>Worked solution</summary>

```scala
def firstPassing(candidates: List[Candidate], threshold: Int): Option[String] =
  candidates.find(_.score >= threshold).map(_.name)
```

This returns the first passing name in input order, not the highest score.
It is O(n) worst case with short-circuiting when a match is found. Test empty,
all failing, first passing, later passing and score exactly at threshold.
Use an immutable department ID or immutable department value if sharing must
be safe. A case-class copy reuses a mutable department reference; it doesn't
clone the department graph.

</details>

## 3. Build a histogram without quadratic List append

Count words using an immutable Map, then produce ordered `(word, count)` pairs.
Explain why repeated `acc :+ element` on List is costly. How would you safely
reduce an empty list?

<details><summary>Worked solution</summary>

The supplied histogram uses:

```scala
words.foldLeft(Map.empty[String, Int]) { (counts, word) =>
  counts.updated(word, counts.getOrElse(word, 0) + 1)
}.toList.sortBy(_._1)
```

For n words and k distinct keys, counting uses expected effectively constant
map operations per word; ordering k keys adds O(k log k) comparison work.
Key lengths/hash quality affect those costs. Each `List :+` traverses/copies
the prefix; repeating it for a growing list yields quadratic work. Prepend
then reverse once, use a builder, or choose a representation suited to appends.
For empty reduction use reduceOption, or foldLeft with the proper identity.
An arbitrary seed is not automatically a lawful reduction identity.

</details>

## 4. Add a promotion without subclass explosion

Introduce a rule that discounts only purchases at or above a threshold,
and a formatter that adds a currency label. How should rounding and invalid
discounts be tested? When would a trait be excessive?

<details><summary>Worked answer</summary>

The supplied ThresholdDiscount already implements the fixed-amount rule;
inject a new ReceiptFormatter for the currency label rather than subclassing
Checkout. Test threshold−1, threshold, zero, invalid threshold/discount and
negative price. For percentage rules, define integer minor-unit rounding and
overflow behavior before coding, then test non-divisible prices. A rule that
returns more than its input violates substitutability despite compiling.
For a one-off stable pure formatter, passing a `Long => String` can be simpler
than introducing an entire named trait hierarchy.

</details>

## 5. Accumulate validation errors

The supplied Either pipeline stops at the first error. You are importing
independent score strings and want every invalid row with its original index.
Return either all valid scores or all indexed errors, without unsafe `.get`.

<details><summary>Worked solution</summary>

One clear two-pass approach (assuming `score` and `InputError` are imported
from `AlgebraicDataTypes`):

```scala
def validateAll(
    rows: List[String]
): Either[List[(Int, InputError)], List[Int]] =
  val checked = rows.zipWithIndex.map { (raw, index) =>
    score(raw).left.map(error => (index, error))
  }
  val errors = checked.collect { case Left(error) => error }
  if errors.nonEmpty then Left(errors)
  else Right(checked.collect { case Right(value) => value })
```

The whole input is validated, order is preserved, and empty input produces
Right(Nil). Time is O(n) plus string parsing, storage O(n). This deliberately
differs from a for-comprehension over Either. Test two invalid rows separated
by a valid row, zero and 100, negative/out-of-range values, malformed numbers
and empty input. If independent fields inside *one* row must also accumulate
errors, don't short-circuit those field checks either.

</details>

## 6. Define an ordering type-class boundary

The supplied Show example has an explicit redacted override. Add a generic
function that returns the maximum of a List using Scala's Ordering type class.
Avoid throwing on empty input and explain how an alternative ordering changes
the answer without changing the underlying data.

<details><summary>Worked solution</summary>

```scala
def maximum[A](values: List[A])(using order: Ordering[A]): Option[A] =
  values.reduceOption((left, right) =>
    if order.gteq(left, right) then left else right
  )
```

This is a one-pass O(n) comparison algorithm. Normal integer ordering makes
`List(1, 3, 2)` return Some(3); supplying `Ordering.Int.reverse` returns Some(1).
Empty returns None and singleton returns its element. If comparing candidates,
define a score ordering plus an explicit tie policy. An alternative instance
changes policy, not the candidate class. Test ordering laws if authoring a
nontrivial comparator: equal comparison, sign symmetry, transitivity and tie
behavior must be coherent.

</details>

## 7. Observe Future semantics without sleeps

Write a test demonstrating that an operation has started without waiting for
its value. Then show how map can produce nested Futures. Does
Future.sequence limit concurrency or cancel siblings after failure?

<details><summary>Worked answer</summary>

Complete a “started” Promise inside the scheduled body; wait on that Promise
at the test boundary. Use another Promise as a gate if work must remain pending.
The supplied lesson follows this pattern. Keep the signal immediately after
the event you claim it proves, use bounded waits, and close the owned executor.

`Future.successful(1).map(n => Future(n + 1))` has type
`Future[Future[Int]]`. Change map to flatMap to obtain `Future[Int]`.
Flattening does not cancel work. Sequence combines results, and the underlying
tasks may already be started; it is not a bounded worker queue and does not
automatically cancel siblings. Await timeout only ends the wait. Use a
deliberately bounded admission policy for large workloads.

</details>

## 8. Final mixed exercise: validated asynchronous score import

**Prompt:** Read a batch represented by a List of strings, validate every row,
then compute feedback for valid batches asynchronously. Keep parsing pure.
Return typed errors for invalid input, preserve input order, and explain which
layer owns execution resources.

Spend 10 minutes designing types, 20 minutes implementing, and 15 minutes
testing/explaining.

<details><summary>Worked design and solution outline</summary>

Reuse `validateAll` from exercise 5. The boundary can have this shape:

```scala
def feedbackBatch(
    rows: List[String]
)(using scala.concurrent.ExecutionContext)
    : scala.concurrent.Future[Either[List[(Int, InputError)], List[String]]] =
  validateAll(rows) match
    case Left(errors) =>
      scala.concurrent.Future.successful(Left(errors))
    case Right(scores) =>
      scala.concurrent.Future {
        Right(scores.map(value =>
          if value >= 70 then "pass" else "practice"
        ))
      }
```

The surrounding scope must provide `InputError` and `validateAll`, just as in
exercise 5. No external I/O is required. Invalid input yields an already
completed Future with typed errors. A valid batch performs a single scheduled
task with an ordered pure map, avoiding one Future per tiny score calculation.
Unexpected nonfatal failures of the scheduled body still fail the Future;
expected domain rejection lives in Either.

Test empty batch, all invalid, mixed errors, bounds 0/100, threshold 69/70 and
result ordering. Use Await only in the test/CLI runner and close only the
executor created by that runner. An injected shared ExecutionContext belongs
to the application, not feedbackBatch.

For a remote service per score, you would need explicit concurrency limits,
timeouts, retry policy, idempotence and error aggregation semantics. `traverse`
alone does not solve capacity limits. Cancellation is a separate requirement,
not something to infer from the Future type.

</details>

## Interview answer rubric

A strong answer should:

1. State types precisely—especially nested List/Option/Either/Future types.
2. Cover empty, invalid and boundary cases rather than reaching for `.get`.
3. Identify where mutation, throwing, scheduling and blocking occur.
4. Explain ordering, evaluation count and resource ownership.
5. Give complexity with representation/function-cost qualifications.
6. Distinguish Scala 3 language features from Scala 2 syntax and from third-party
   frameworks. None of these examples require Spark, Cats or an effect library.
