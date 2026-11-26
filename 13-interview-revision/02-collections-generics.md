# Collections, hashing, ordering, and generics

## Goal and flow

Choose a collection from the access pattern, mutation pattern, ordering
contract, and concurrency requirement—not from habit.

```text
key/value operation
  -> hash(key)
  -> bucket index
  -> equality check among candidates
  -> value update or insertion
  -> resize/treeify when thresholds require it
```

Run the collection lesson:

```bash
cd 01-core-java
javac --release 17 -Xlint:all -Werror -d out \
  src/main/java/com/interviewprep/core/*.java
java -cp out com.interviewprep.core.CollectionChoices
java -cp out com.interviewprep.core.LocksAndConcurrentMaps
```

## Collection decision table

| Need | Default choice | Important caveat |
|---|---|---|
| Indexed access and append | `ArrayList` | Middle insertion shifts elements |
| Queue/deque | `ArrayDeque` | No `null`; excellent end operations |
| Hash lookup | `HashMap` | Requires stable key equality/hash |
| Insertion order | `LinkedHashMap`/`LinkedHashSet` | Extra links and memory |
| Sorted keys | `TreeMap`/`TreeSet` | `O(log n)` and comparator consistency |
| Many readers, rare writes | `CopyOnWriteArrayList` | Every write copies the array |
| Concurrent map updates | `ConcurrentHashMap` | No `null` keys or values |

## Interview questions and answers

### How does `HashMap.put` work?

**Answer:** Java hashes the key, spreads high bits, computes a bucket index,
then compares keys using hash and `equals`. A collision is stored in the bucket;
modern Java can treeify a sufficiently long bucket after the table is large
enough. When size exceeds capacity times load factor, the table resizes and
entries move to new bucket positions. Average lookup is `O(1)`, but the
contract depends on stable `equals`/`hashCode`.

**Trap:** “HashMap is always O(1)” ignores collisions, resizing, poor hashes,
and adversarial keys.

### Why can `ConcurrentHashMap` not store `null`?

**Answer:** A missing key returns `null`. If `null` values were allowed, a
reader could not distinguish “no mapping” from “mapped to null” without another
operation. The restriction also keeps atomic compute/merge semantics
unambiguous.

### Fail-fast versus fail-safe iterator?

**Answer:** A fail-fast iterator detects structural modification through a
modification count and may throw `ConcurrentModificationException`; it is a
bug detector, not a thread-safety guarantee. A snapshot/weakly consistent
iterator, such as one from a concurrent collection, has a different visibility
contract and may not show every concurrent update. “Fail-safe” is informal API
language, not a universal Java guarantee.

### Explain PECS.

**Answer:** Producer Extends, Consumer Super. Read `? extends T` when the
collection produces `T` values; write `T` values into `? super T` when it
consumes them. A wildcard captures an unknown type, so `List<? extends Number>`
cannot safely accept an arbitrary `Number`, while `List<? super Integer>` can
accept an `Integer`.

```java
static double sum(List<? extends Number> values) {
  return values.stream().mapToDouble(Number::doubleValue).sum();
}

static void addDefaults(List<? super Integer> values) {
  values.add(0);
}
```

### Why can’t you create a generic array?

**Answer:** Arrays retain runtime component type and are covariant; generics
use type erasure. `new T[10]` would not know the runtime component type, and
allowing it could create heap pollution. Use `List<T>`, pass a component
`Class<T>`, or create an erased array only behind a carefully checked boundary.

### How do you implement an LRU cache?

**Answer:** Use an access-ordered `LinkedHashMap` and override
`removeEldestEntry`. The map gives near-constant lookup/update and the linked
list tracks recency. It is not automatically thread-safe and does not provide
distributed cache semantics.

```java
Map<String, String> cache = new LinkedHashMap<>(16, 0.75f, true) {
  @Override
  protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
    return size() > 100;
  }
};
```

## Code/config walkthrough

`CollectionChoices.java` compares ordering, views, and deque behavior.
`LocksAndConcurrentMaps.java` demonstrates atomic `merge` and lock ownership.
The [storage guide](11-storage-cloud-devops.md) connects this in-process LRU
idea to Redis TTL and cache invalidation.

There is no collection-specific configuration file: capacity and load factor
are code-level choices. For production caches configure maximum size, expiry,
serialization, metrics, and eviction behavior explicitly.

## Failure exercise

Create a key whose field changes after insertion. Then:

1. Insert it into `HashMap`.
2. Mutate the field used by `hashCode`.
3. Call `get` with the same object.
4. Explain why the entry is still physically present but lookup uses a new
   bucket.

## Revision card

```text
ArrayList is contiguous; ArrayDeque is the normal queue/deque.
HashMap requires stable equality state and resizes at a threshold.
TreeMap trades constant-time lookup for sorted O(log n) operations.
CopyOnWrite is for many readers and few writes.
ConcurrentHashMap gives atomic map operations, not a transaction.
PECS describes who produces and consumes values.
Type erasure is why generic arrays are unsafe.
```
