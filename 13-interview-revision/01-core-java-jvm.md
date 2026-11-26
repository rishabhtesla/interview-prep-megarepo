# Core Java, JVM, and object design

## Goal and mental model

Refresh the language before discussing Spring. Java source becomes bytecode;
the class loader loads it, the JVM verifies and executes it, objects live on
the heap, each thread has a stack, and the garbage collector reclaims
unreachable heap objects. Java always passes arguments by value: for an object,
the copied value is a reference to the same object.

## Flow

```text
source.java
  -> javac
  -> bytecode.class
  -> class loader -> verification/linking/initialization
  -> interpreter/JIT
  -> heap objects + thread stacks
  -> GC reclaims unreachable objects
```

## Run the refresher

From `01-core-java`:

```bash
javac --release 17 -Xlint:all -Werror -d out \
  src/main/java/com/interviewprep/core/*.java
java -cp out com.interviewprep.core.ZeroToHero
java -cp out com.interviewprep.core.JvmAndValues
java -cp out com.interviewprep.core.ModernJava17
```

Read [the Java learning guide](../01-core-java/LEARNING_GUIDE.md) for the
complete lesson order. `ZeroToHero` is the first pass; `JvmAndValues`,
`TypesGenericsExceptions`, `ObjectDesign`, and `ModernJava17` are the interview
anchors.

## JVM memory and failures

| Area | Owns | Typical failure |
|---|---|---|
| Heap | Objects and arrays shared by threads | `OutOfMemoryError: Java heap space` |
| Thread stack | Frames, local variables, return addresses | `StackOverflowError` from unbounded recursion |
| Metaspace | Class metadata and class-loader structures | Metaspace exhaustion from class-loader leaks |
| PC register | Current instruction per thread | Not normally configured directly |
| Native stack | Native/JNI calls | Native memory exhaustion or native crash |

Young-generation collection copies or evacuates short-lived objects; surviving
objects may be promoted. Old-generation collection is more expensive. Serial,
Parallel, G1, and ZGC make different throughput/latency trade-offs; the
collector is not a correctness mechanism.

## Interview questions and answers

### Abstract class versus interface

**Answer:** An abstract class can hold instance state, constructors, protected
implementation, and one superclass relationship. An interface defines a
capability contract and can contain abstract, default, static, and private
methods, but it does not provide per-instance fields. Use an interface for a
replaceable role and an abstract class only when implementations genuinely
share state or lifecycle.

**Trap:** Default methods do not create multiple class inheritance. If two
interfaces provide the same default method, the implementing class must resolve
the conflict explicitly; a more specific interface wins when the hierarchy
unambiguously provides one.

### Why is Java pass-by-value?

**Answer:** The callee receives a copy of every argument. For a primitive, the
copy is the primitive value. For an object, the copied value is a reference, so
mutating the referenced object is visible to the caller but reassigning the
parameter is not.

```java
static void change(List<String> values) {
  values.add("mutated");       // caller sees this
  values = new ArrayList<>();  // caller does not see this rebinding
}
```

### What is the `equals`/`hashCode` contract?

**Answer:** Equal objects must have equal hash codes, equality must be
reflexive/symmetric/transitive/consistent, and `equals(null)` is false. A
mutable field used in either method can make a `HashMap` entry unreachable
after insertion. Use immutable identity fields for map keys.

### What is the difference between `OutOfMemoryError` and `StackOverflowError`?

**Answer:** Heap/metaspace/native allocation failure is an `OutOfMemoryError`;
exhausting one thread's stack frames is a `StackOverflowError`. Diagnose the
first with GC logs, native-memory tracking, class-loader analysis, and heap
dumps. Diagnose the second with the stack trace and recursion/thread-stack
configuration. Do not catch either as normal business validation.

### How do you investigate a production memory leak?

**Answer:** Compare heap histograms across time, take a safe heap dump, find the
dominator retaining unexpected objects, inspect GC roots, and correlate the
retention path with request/thread/class-loader lifetime. A growing cache,
`ThreadLocal`, listener, static collection, or class-loader often owns the
objects. Fix ownership; increasing `-Xmx` only delays the failure.

### Why is `String` immutable?

**Answer:** Immutability makes strings safe to share, cache their hash code, use
as map keys, and place in the string pool. A custom immutable class needs final
class or controlled inheritance, final fields, constructor validation,
defensive copies for mutable inputs/outputs, no mutators, and equality based on
stable state.

### Strong, weak, soft, and phantom references?

**Answer:** A strong reference keeps an object alive. A weak reference does not
prevent collection and is useful for canonical maps or listeners; `WeakHashMap`
removes entries whose keys are no longer strongly reachable. A soft reference
may be cleared under memory pressure but is not a reliable cache policy.
Phantom references work with a `ReferenceQueue` for cleanup tracking and never
return the referent. Use explicit lifecycle/close methods for important
resources.

### What does the String Constant Pool do?

**Answer:** The JVM can share equal string literals in a pool. `intern()` asks
for the canonical pooled representation, but excessive interning increases
pool pressure and is not a substitute for a bounded cache. String equality
uses `equals`, not `==`; `==` only compares references.

### `final`, `finally`, and `finalize()`?

**Answer:** `final` restricts reassignment, overriding, or inheritance depending
on where it appears. `finally` is a cleanup block. `finalize()` was an
unreliable, nondeterministic cleanup hook and is deprecated; use
`AutoCloseable`/try-with-resources or a carefully bounded `Cleaner` fallback.

### Comparable versus Comparator?

**Answer:** `Comparable<T>` defines a type's natural ordering;
`Comparator<T>` defines an external ordering/policy. Implement `Comparable`
when one obvious stable order exists and use comparators for alternate views.
Ordering should be consistent with equality when used by `TreeSet`/`TreeMap`.

### Checked exception, unchecked exception, and `Error`?

**Answer:** Checked exceptions are part of a compile-time method contract;
unchecked exceptions usually indicate programming/validation or runtime
boundary failures; `Error` represents serious JVM/system conditions. Try-with-
resources closes resources in reverse declaration order and preserves close
failures as suppressed exceptions when the body already failed.

### Serial, Parallel, G1, and ZGC?

**Answer:** Serial favors small heaps and simple single-threaded collection.
Parallel favors throughput with parallel stop-the-world work. G1 uses regions
to target more predictable pauses while balancing throughput. ZGC targets very
low pauses with mostly concurrent work. Choose from measured pause,
throughput, and heap requirements; no collector repairs a retained reference.

### What are class loaders and reflection trade-offs?

**Answer:** Bootstrap loads platform classes, the platform loader loads platform
modules, and the application loader loads application classes. Parent-first
delegation prevents an application from silently replacing core classes.
Reflection enables frameworks and dynamic adapters but weakens compile-time
checks, can bypass encapsulation, increases startup/runtime overhead, and
requires explicit access/security review.

### How do records, sealed classes, and pattern matching help?

**Answer:** Records make value-oriented data carriers concise and provide
component accessors, equality, and a canonical constructor, while still
requiring defensive copies/validation for mutable inputs. Sealed classes limit
which types can implement a hierarchy. Pattern matching makes handling known
variants explicit and easier to review. These features improve domain modeling;
they do not replace invariant validation or persistence identity.

## Code walkthrough

`ObjectDesign.java` demonstrates equality and composition; `JvmAndValues.java`
shows initialization and reference copying; `TypesGenericsExceptions.java`
shows overflow, boxing, array covariance, and suppressed exceptions. Read the
assertion immediately after each action: it states the contract more clearly
than a comment alone.

## Configuration and failure exercise

Optional observations:

```bash
java -Xlog:class+load=info -cp out com.interviewprep.core.JvmAndValues
java -Xlog:gc -cp out com.interviewprep.core.CoreJavaDemo
javap -classpath out -c -p com.interviewprep.core.JvmAndValues
```

Change a mutable field used by a key's `hashCode`, insert the key into a
`HashMap`, mutate it, and try to retrieve it. Explain why the map did not lose
the object but can no longer find it in the expected bucket.

## Revision card

```text
Java passes values; object values can point to shared mutable state.
Heap, stack, metaspace, PC, and native stack have different ownership.
GC reclaims unreachable objects; it does not fix a retained reference.
equals/hashCode must use stable equality state.
Interfaces model roles; classes model shared state/lifecycle.
Diagnose errors from evidence instead of catching Error as business logic.
```
