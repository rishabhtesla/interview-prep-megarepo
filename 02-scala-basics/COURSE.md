# Scala foundations: a complete interview study sequence

Read the sources after each section and use the [practice lab](EXERCISES.md).
All runnable examples use the pinned Scala 3 configuration in this track;
commands are in [README](README.md).

## 1. Values, functions and evaluation

**Source:** `ValuesAndFunctions.scala`

Scala encourages programs built from expressions and immutable values.
`val count = 1` fixes a binding; `var count = 1` allows reassignment. Neither
keyword changes whether the referenced object itself can mutate. A
`val ArrayBuffer` can still gain elements. Prefer immutable data at API
boundaries and keep any necessary mutation small, owned and explicit.

An `if` or `match` returns a value, avoiding an initially unassigned variable.
A method `def classify(score: Int): String` has parameters and a body. A value
of type `Int => String` can be passed to another function, stored or returned.
Scala can convert methods to function values when needed; that does not mean
every method call allocates a closure.

### Types you should recognize

| Type | Meaning / caveat |
|---|---|
| `Int`, `Long`, `Double` | Numeric values with JVM representation/boxing details |
| `String` | JVM String, immutable but may contain multi-code-unit characters |
| `Unit` | One meaningful result `()`: “no useful value,” not Java null |
| `Nothing` | Bottom type with no normal values; useful for throw and empty covariant containers |
| `Any` | Broad top type; reaching for it loses useful static constraints |
| `A => B` | Function from A to B |
| `Option[A]` | Zero or one A, explicit absence |
| `(A, B)` | Tuple containing both an A and a B |

Ordinary integer arithmetic can overflow just as in Java. `BigInt` in the
factorial example avoids fixed-width overflow, at the cost of increasing
arithmetic and storage requirements. Tail recursion makes call-stack use
constant, **not** the total memory of a growing arbitrary-precision result.
`@tailrec` asks the compiler to verify that recursive calls can be rewritten;
it is not an optimizer for arbitrary recursion.

**Q: How do call-by-value, by-name and lazy val differ?**

- A normal parameter evaluates its argument before the method call.
- A by-name parameter (`value: => A`) evaluates the expression on each access;
  unused parameters need not run at all.
- A `lazy val` evaluates on its first access and caches a successful result.
  It is not the same as an ordinary def, which can rerun its body.

The lesson evaluates a by-name `next()` twice and gets `1 + 2 = 3`.
Passing a lazy value backed by the next call gets `3 + 3 = 6` while incrementing
the counter only once. This is why a parameter name alone does not establish
whether an effect runs once or repeatedly.

**Q: What is referential transparency?** Replacing an expression with its value
does not change observable behavior. `factorial(5)` is a pure calculation;
`next()` mutates state, so replacing repeated calls by one cached value changes
results. Prefer a pure calculation core with explicit effectful boundaries.
Immutability helps but does not make file I/O, clock access or random generation
pure automatically.

**Pitfalls:** Missing an else can produce a less useful inferred result type;
mixing unrelated branch types can widen types; ordinary recursive calls may
overflow the stack; using mutable collections behind `val` does not establish
thread safety. Annotate public return types to keep API intent stable.

## 2. Case classes and pattern matching

**Source:** `CaseClassesAndMatching.scala`

A case class represents a product: `Candidate(name, score)` contains both
fields. It supplies construction/extraction support, accessors, copy,
value-oriented equals/hashCode and a readable toString. Case classes can be
pattern-matched without manual getter and cast code. Prefer immutable
constructor parameters; a case class with mutable fields is legal but creates
identity and sharing hazards.

Pattern matches are checked top-to-bottom. A guard such as `if score >= 85`
is an extra condition after a structural pattern succeeds. Put specific cases
before general fallbacks. `first :: second :: _` decomposes the first two List
cells; it doesn't require traversing the whole list. The fallback handles both
an empty and singleton list.

**Q: Is copy a deep clone?** No. Copy creates a new outer value and reuses
unchanged component references. If a component contains mutable data, the old
and new values can still share it. The lesson's String/Int components avoid
that alias. Constructor `require` checks are rerun by copy, so an invalid
score cannot bypass construction merely by copying a valid candidate.

**Q: Why is an incomplete match dangerous?** It may throw MatchError at runtime.
The compiler can warn for many incomplete matches over closed structures,
but a broad wildcard can hide newly added variants or mistakes. In a closed
domain ADT prefer explicit cases when you want future variants to trigger
review. This course treats compiler warnings as errors.

**Q: Can I use a case class as a map key?** Yes, when the relevant equality
components have stable value semantics. Equal keys have equal hashes. Mutable
components or unusual Java collections/arrays can violate your intended
semantics; arrays have reference-oriented equality rather than recursively
comparing contents by default.

**Pitfall:** Lowercase names inside patterns generally bind variables; they do
not compare against an existing local value unless you use a stable identifier
pattern, for example backticks. `case expected =>` catches everything and binds
a new name; ``case `expected` =>`` compares with that existing value.

## 3. Collections, transformations and folds

**Source:** `CollectionPipelines.scala`

Scala's default `List`, `Vector`, `Map` and `Set` imports emphasize immutable
collections. “Updating” creates a new value, often sharing unchanged structure.
This is not the same as copying every element on every operation. Mutable
collections exist explicitly in `scala.collection.mutable`; choose them for
an owned builder or demonstrated performance need, not by habit.

### Follow the types, not just the method names

For `inputs: List[String]` and `parse: String => Option[Int]`:

- `inputs.map(parse)` is `List[Option[Int]]`: one result slot per input.
- `inputs.flatMap(parse)` is `List[Int]`: Option contributes zero or one element.
- `values.filter(_ > 2)` retains only matching elements, preserving order.
- `values.reduce(_ + _)` has no supplied seed and fails on empty input.
- `values.reduceOption(_ + _)` returns None for empty input.
- `values.foldLeft(0)(_ + _)` gives a defined identity/empty result of zero.

**Q: Why can foldLeft and foldRight differ?** The association differs.
For `[4,2,4]`, left subtraction with zero is `((0 - 4) - 2) - 4 = -10`.
Right subtraction is `4 - (2 - (4 - 0)) = 6`. A sequential foldLeft documents
an order; regroupable reductions need an associative operation and a correct
identity. A seed of 10 isn't an identity for addition, even if a sequential
calculation with that starting balance is intentional.

**Q: Does for mean a loop?** A for/yield comprehension translates to the
type's map/flatMap/withFilter methods. For List, multiple generators produce
combinations; for Option, any None short-circuits; for Either, Left
short-circuits; for Future, it describes dependent composition. Its semantics
come from the participating type, not a universal imperative loop rule.
Use zip when you want element-by-position pairs.

### Complexity reference

| Collection / operation | Typical cost | Important qualification |
|---|---|---|
| List head/prepend/tail | O(1) | Tail shares the original nodes |
| List index, append, length | O(n) | Repeated append in a fold can become O(n²) |
| Vector index/update/append | Effectively constant for practical sizes | Tree depth/logarithmic structure, not a literal flat-array model |
| Immutable HashMap lookup/update | Expected effectively constant | Hashing/equality/collision and trie structure costs still matter |
| TreeMap lookup/update | O(log n) | Requires an ordering; comparator cost is additional |
| List map/filter/fold | O(n) | Function cost and output allocation are additional |
| List flatMap | O(n + total emitted elements) | A function can emit many items per input |
| Sort | Generally O(n log n) | Extra representation/storage costs depend on API |

Small immutable Maps/Sets can use specialized representations; do not confuse
an abstract contract with one current implementation. To build a List in one
pass, prepend and reverse once or use a builder rather than repeated append.
Vector often suits indexed workloads; List suits head/tail decomposition.

Strict transformations compute immediately. A view defers and can recompute
on repeated traversal; it doesn't cache the result. An Iterator is stateful
and one-shot. LazyList memoizes evaluated elements, which can retain substantial
memory if its head stays reachable. The lesson checks exact visits for a
view prefix without depending on clocks or performance measurements.

**Pitfalls:** `head`, `tail`, `reduce` and direct Map lookup can fail on empty
or missing input. Prefer headOption/get/reduceOption when absence is valid.
Hash-map iteration is not a sorted-order promise. Sorting just to obtain a
minimum does more work than a one-pass minimum operation.

## 4. Classes, traits and composition

**Source:** `ClassesAndTraits.scala`

A class owns state and implementation. A trait describes a reusable role and
can contain abstract and concrete members. An object is a singleton value and
can be a companion with construction helpers/instances. Scala 3 supports trait
parameters, but these examples don't need them.

The checkout depends on `PriceRule` and `ReceiptFormatter` traits. The rule can
be changed independently of formatting. Tests inject a local formatter instead
of touching global state or subclassing checkout. A threshold discount
requires nonnegative parameters and a discount not greater than its threshold,
so a valid nonnegative price cannot become negative.

**Q: How does inheritance differ from a type class?** A trait used via
subtyping says the object itself implements the role. A type class, explored
later, supplies behavior *about* a type as separate evidence. Subtyping is
natural when each object has one intrinsic role; type classes can add behavior
to external types or select alternate policies without changing the type.
Neither is automatically superior.

**Q: What is substitutability here?** Every PriceRule must return a value
between zero and input price. A rule that adds money satisfies the method type
but violates the behavioral contract. The example detects that violation.
Boundary tests at threshold−1, threshold and zero matter more than tests only
at arbitrary happy-path values.

Trait composition has a linearization order used to resolve inherited behavior;
mixing order matters, especially with `super`/stackable traits. Rather than
memorizing slogans about “last trait wins,” trace the actual linearization and
explicit overrides in the code under discussion. Avoid using that mechanism
for hidden application policy if constructor-injected collaborators are clearer.

**Pitfalls:** Calling overridable members during construction can observe
uninitialized subclass state. Traits with mutable fields and initialization
side effects create subtle ordering dependencies. Use explicit parameters,
small contracts and composition unless inheritance clearly models the domain.

## 5. Functional ADTs, Option, Either and Try

**Source:** `AlgebraicDataTypes.scala`

An algebraic data type combines **products** (all fields in a case) and **sums**
(one alternative from several). `InputError` is either NotAnInteger with raw
text or OutOfRange with an integer. `Decision` is Accepted or Rejected.
Scala 3 enums model these closed alternatives; sealed traits plus case classes
are another common encoding.

This distinguishes three outcomes:

1. The input could not be parsed.
2. The integer violates the valid score range.
3. The input is valid but the business decision is rejection.

Conflating those cases into false/null makes callers guess. A typed result
preserves the meaning through a pipeline.

| Type | Best fit | Limitation |
|---|---|---|
| `Option[A]` | Expected presence/absence | None carries no reason |
| `Either[E, A]` | Expected typed failure or success | Standard flatMap stops at first Left |
| `Try[A]` | Capture nonfatal thrown exceptions at an effectful boundary | Error is Throwable, not a domain-specific ADT |

**Q: What does right-biased Either mean?** map/flatMap operate on Right and
carry Left through unchanged. In the lesson, parsing runs before range
validation. A parse failure prevents later stages from running. This is
fail-fast validation, not accumulation of every independent error.

**Q: Should every exception become a Left or None?** No. Translate only the
exceptions your boundary promises to interpret. `Try` catches `NonFatal`, not
every Throwable; serious errors and interruption-like control conditions need
their own semantics. The legacy adapter converts NumberFormatException to a
typed parse error and does not hide unexpected failures. It only parses:
domain range checking remains a separate operation.

`Option(null)` gives None; `Some(null)` still constructs a present null in the
normal Scala 3 nullability mode used here. Explicit-null checking is a separate
compiler mode, not enabled by this course. `toOption` from Either discards
error information intentionally; use it only when that loss is acceptable.

**Q: What makes a match useful for evolution?** A match over a closed enum with
explicit variants can become non-exhaustive when the domain grows. The
compiler warning draws attention to code that needs updating. A catch-all may
silence that signal. No type system checks your business threshold automatically;
test boundary values in addition to compiler exhaustiveness.

**Pitfalls:** Option.get/Try.get bypass safe composition. Throwing inside
Either.map still throws; Either doesn't automatically catch exceptions.
Overusing exceptions for normal rejected input obscures expected control flow.
Use the pure `score` and `decide` functions as the core, with I/O around them.

## 6. Contextual parameters and type classes

**Source:** `ContextualParameters.scala`

A type class is a normal parameterized interface plus implementations
(*instances*) and a way to provide the right instance. `Show[A]` knows how to
render A. A method taking `(using Show[A])` asks the compiler to supply
evidence from contextual scope; no reflection is needed.

```scala
def render[A](value: A)(using instance: Show[A]): String =
  instance.show(value)
```

Scala 3 `given` declares an instance; `using` requests or explicitly supplies
one. `summon[Show[A]]` obtains available evidence. `[A: Show]` is a context-bound
shorthand. An extension method can make a helper look like a method on A,
without modifying A's class or its runtime method table.

**Q: How can List rendering work for any element type?** The generic instance
`given [A](using Show[A]): Show[List[A]]` requests element rendering evidence,
then maps and joins the elements. `Show[List[Int]]` can be built because
`Show[Int]` exists. If no appropriate element instance exists, compilation
fails rather than guessing how to render unknown data.

Instances in the type class companion participate in contextual lookup.
Elsewhere, `import Instances.given` explicitly imports givens; don't assume an
ordinary wildcard import is always the equivalent. Keep policies small in
scope. Two equally eligible instances can cause a compile-time ambiguity,
not a runtime random selection.

**Q: Why explicitly pass a redacted instance?** Formatting can be policy:
public logging may need to hide a name while an internal screen displays it.
`render(candidate)(using redacted)` makes that sensitive choice visible.
Changing a global instance would affect unrelated call sites. The lesson
checks that an explicit override does not mutate default rendering.

**Q: Is every given a type class?** No. A contextual ExecutionContext or request
configuration is a contextual dependency but may not encode a type-indexed
behavior such as Show[A]. Distinguish language syntax from the design pattern.

**Pitfalls:** Deep contextual searches and very broad imports can make errors
hard to understand. Begin by spelling out the evidence parameter, inspect the
required type, and only then use shorthand. Type correctness alone doesn't
establish algebraic laws; if defining Ordering, test comparison consistency.
The sample Show is educational text rendering, not escaped JSON or a secure
serialization protocol.

## 7. Future, execution contexts and effect boundaries

**Source:** `FuturesAndEffects.scala`

`Future { body }` schedules the body eagerly on an ExecutionContext.
The resulting Future holds one eventual completion; observing it twice does
not rerun the body. A method that constructs a *new* Future on every call is
different from storing one Future value. Future is not a lazy, repeatable,
generally cancellable effect description.

The runnable lesson owns a fixed-thread executor and passes it as contextual
ExecutionContext evidence. Promise is the writable, single-completion side;
Future is its read side. Promises coordinate the demonstration without sleeps:
the “started” promise proves the eager task ran, and a gate controls downstream
completion.

**Q: What's the difference between map and flatMap on Future?**

- map turns `Future[A]` and `A => B` into `Future[B]`.
- flatMap turns `Future[A]` and `A => Future[B]` into one `Future[B]`.
- Using map with an async-returning function creates `Future[Future[B]]`.

For-comprehensions express the same operations. Compare:

```scala
val first = fetchA()
val second = fetchB()
val combined = for
  a <- first
  b <- second
yield (a, b)
```

with:

```scala
val combined = for
  a <- fetchA()
  b <- fetchB()
yield (a, b)
```

In the first, both fetch methods are called before composition, so independent
work can overlap. In the second, the second call is inside a flatMap callback
and follows the first successful result. These fragments assume the fetch
methods return Futures; they are conceptual sketches, not extra runnable files.

**Q: Does Future.sequence preserve completion order?** For the input List in
the example it preserves input order, even if tasks finish differently.
One failure fails the combined Future; it does not cancel the other started
tasks. `Future.traverse` can start a large amount of work eagerly; it is not
an automatic bounded-concurrency worker pool.

**Q: How does error recovery work?** `recover` converts matching failures to a
value. `recoverWith` returns a replacement Future and flattens it. Use specific
exception patterns; a blanket fallback can turn an outage into plausible but
wrong data. Exceptions thrown by ordinary Future transformations generally
fail the resulting Future when nonfatal; distinguish this from raw Either.map.

### Blocking and lifecycle

Use Await only at a controlled executable/test boundary here. Blocking inside
worker callbacks can starve an executor, especially when waiting on work
queued to the same pool. Nonblocking composition avoids that waiter. For real
blocking I/O, use a deliberately managed blocking executor or an appropriate
asynchronous API. `scala.concurrent.blocking` is a hint to supporting contexts,
not permission to block indefinitely or a guarantee that every executor grows.

The global execution context is convenient but not a universal resource policy.
The fixed pool here bounds worker count, **not its queue length**, and serves
only a handful of educational tasks. A production service needs a bounded
admission/backpressure strategy and queue/latency metrics. The creator owns
shutdown and termination; injected application-wide contexts shouldn't be
closed by each helper function.

**Q: If Await times out, did computation stop?** No. The wait ends, not the
Future's underlying work. Standard Scala Future has no general cancel
operation. If cancellation/resource safety are requirements, design a
cooperative protocol or choose an appropriate effect/runtime library after
evaluating its semantics. Such libraries are outside this dependency-free track.

## 8. Testing, debugging and interview reasoning across lessons

The explicit Checks helper fails even if compiler assertions are disabled.
It tests values and semantics, not wall-clock speed. Functional lessons use
boundary/empty/error cases. The Future lesson uses Promise and bounded Await,
propagates failures, and always closes the owned executor. These checks are
not stress tests, exhaustive proofs or a measurement of real I/O latency.

When a type error is difficult, annotate intermediate types:

```scala
val mapped: List[Option[Int]] = inputs.map(parseNonNegative)
val flattened: List[Int] = inputs.flatMap(parseNonNegative)
```

Then check the expected function shape before adding casts. A cast can silence
the compiler without fixing a wrong abstraction. For contextual errors, write
the missing `Show[SpecificType]` or explicit using argument to localize lookup.
For match problems, inspect which pattern caught the value and whether a
lowercase pattern accidentally introduced a binder.

For runtime debugging, reduce to a pure function with a small input, test the
first broken invariant, and inspect asynchronous failures by observing the
returned Future rather than relying on println from callbacks. Worker thread
names and logs don't establish an ordering contract. Avoid println-based
microbenchmarks: Scala shares JVM warm-up, JIT, allocation and GC concerns.
Explain complexity, representation, boxing and captured closures before
claiming performance. Prefer a readable correct loop/fold first; measure a
representative workload before making collection or concurrency changes.
