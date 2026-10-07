# 1. Execution, planning, and performance

## 1.1 From a SQL expression to machines doing work

The **driver** constructs plans and coordinates execution; **executors** execute
tasks and hold cached/shuffle data. A cluster manager allocates resources but
does not choose your SQL join algorithm. In `local[2]`, driver and execution
share a JVM with two task threads. It cannot reproduce network failures,
multi-executor memory pressure, or realistic cluster scheduling.

A DataFrame transformation normally records a plan, not a new materialized table.
`select`, `filter`, `join`, `groupBy(...).agg(...)`, and `orderBy` are lazy.
`collect`, `count`, writes, and `show` trigger execution. Metadata work can happen
earlier: schema inference and path listing are not guaranteed to wait for an
action. The course avoids CSV inference so its schema is explicit.

The logical word-count DAG is:

```text
text scan → lowercase/split → explode → discard empty tokens
          → partial counts → shuffle by word → combine counts
          → range shuffle/sort → show a bounded result
```

Spark divides distributed execution into **stages** around shuffle dependencies.
A **task** handles one partition for a stage. A job may have many stages; an
application may have many jobs. Do not state that one line, action, or SQL query
always equals exactly one stage or job: broadcast collection, adaptive subqueries,
range sampling, and `take`/`show` can cause auxiliary work.

**Narrow dependency:** each child partition needs a bounded set of parent
partitions without all-to-all redistribution, as in a map/filter pipeline.
Consecutive narrow operations can be pipelined within a task.
**Wide dependency:** data from many parents must be reorganized, commonly for
grouping or repartitioning by key. Wide operations incur serialization, shuffle
files, network/disk transfer, and potentially sort/hash-state memory.

An interview-quality answer draws the DAG and identifies the *required data
movement*, rather than memorizing “join is wide.” A broadcast join can avoid
shuffling the large side; a suitably partitioned join may reuse partitioning.

### Small RDD comparison

In a PySpark shell configured with the course interpreter:

```python
lines = spark.sparkContext.textFile("data/lines.txt")
import re
pairs = lines.flatMap(lambda s: re.findall("[a-z]+", s.lower())).map(lambda w: (w, 1))
counts = pairs.reduceByKey(lambda a, b: a + b)
print(sorted(counts.collect(), key=lambda pair: (-pair[1], pair[0])))
```

This tiny `collect` is safe only because the vocabulary is bounded by the fixture.
`reduceByKey` can combine locally before shuffle; `groupByKey` materializes all
values per key and usually moves more data for a simple sum. RDD lineage enables
recomputation but arbitrary Python lambdas are opaque to Catalyst.

## 1.2 DataFrame, Dataset, RDD: choose the representation deliberately

| Representation | Strength | Cost / limitation |
|---|---|---|
| SQL / DataFrame | Schema-aware relational optimization, built-in expressions, columnar sources | Dynamic column names/type errors can appear at analysis or execution. |
| Scala `Dataset[T]` | JVM type-safe objects with encoders; useful typed APIs | Object conversions and typed lambdas can reduce relational optimization opportunities. |
| RDD | Explicit low-level control, irregular transformations | No Catalyst optimization of arbitrary functions; serialization and manual tuning burden. |

Scala DataFrame is `Dataset[Row]`; PySpark does not provide the same JVM typed
`Dataset[T]` API. PySpark built-in DataFrame expressions are instructions for JVM
execution; they do **not** inherently execute one Python function per row.

Prefer `sum`, `when`, `regexp_replace`, array/map functions, and SQL expressions
over a Python UDF when they express the logic. Regular Python UDFs cross a
language/serialization boundary and are opaque to SQL optimization. Pandas UDFs
can amortize transfer using Arrow batches, but still introduce Python execution,
extra memory, and their own null/type semantics. Arrow is not “free” and a UDF
does not automatically become faster because it is vectorized.

Scala UDFs avoid the Python boundary but remain opaque compared with native SQL
expressions. Native functions are a first choice, not an absolute prohibition
on UDFs. Benchmark a genuinely necessary UDF with representative data.

## 1.3 Catalyst, Tungsten, and adaptive execution

Explain the phases in order:

1. **Unresolved logical plan:** names and operations from user code.
2. **Analysis:** resolve tables, columns, functions, and types against catalogs.
3. **Logical optimization:** simplify expressions, push supported predicates,
   prune columns, and apply relational rewrites.
4. **Physical planning:** choose implementations such as hash aggregation,
   sort-merge join, or broadcast hash join using available statistics and rules.
5. **Execution:** execute operators and generated code; AQE may change remaining
   execution decisions using observed shuffle statistics.

**Catalyst** is the SQL analysis/optimization framework.
**Tungsten** describes execution improvements including efficient binary layouts,
memory management, and generated code. It is not a switch that moves all data
off heap. Whole-stage code generation fuses compatible operators into generated
JVM loops; not every operator/source/UDF participates.

**AQE** in Spark 3.5 can coalesce small post-shuffle partitions, change a join
strategy when runtime sizes justify it, and split certain skewed join partitions.
It does not make every group-by hot key parallel, remove a genuinely required
shuffle, or eliminate executor OOM caused by an enormous single record/group.
Hints and configuration can restrict choices. Inspect initial and final adaptive
plans and actual metrics, not merely `spark.sql.adaptive.enabled=true`.

Run the plan lab and identify:

* `BroadcastExchange` and `BroadcastHashJoin`: a small dimension is distributed.
* Partial/final aggregation: local combination reduces shuffled rows.
* Hash partitioning exchange: grouping keys must meet.
* Sort/range exchange: a global order differs from sorting inside each partition.
* `InMemoryTableScan`: a cached result can be reused after materialization.

`explain("formatted")` highlights operators; `explain("extended")` also shows
logical plans. Explaining a lazy plan is not a substitute for running it and
checking the SQL/Spark UI. A cached adaptive plan may show wrappers rather than
a simple final tree.

## 1.4 Joins: correctness before algorithm selection

Start with grain and cardinality. The course fact grain is **one order ID**;
the dimension grain is **one customer ID**. A duplicate dimension row can
multiply revenue even when Spark executes the join perfectly.

| Join | Keeps | Common use / pitfall |
|---|---|---|
| Inner | Matching pairs | Unknown keys disappear unless explicitly checked. |
| Left outer | All left rows; null right attributes if missing | Filtering right columns afterward may unintentionally turn it into an inner join. |
| Left semi | Left rows that have at least one match | Existence filtering without right-column duplication. |
| Left anti | Left rows with no match | Referential-integrity and missing-dimension checks. |
| Full outer | Matched and unmatched on both sides | Reconciliation; carefully distinguish null keys and missing sides. |

**Broadcast hash join** builds a hash relation from a small side and distributes
it to executors. It avoids repartitioning the large side *for that join*; it does
not imply the entire pipeline is shuffle-free. Fit is about serialized and
deserialized size, executor concurrency, and driver/broadcast pressure, not
“only a million rows.” Default automatic broadcast selection uses statistics
and a size threshold; an explicit hint is not a memory safety guarantee.

**Sort-merge join** usually repartitions both large sides by join key and sorts
them, unless useful partitioning/order already exists. It can spill, but is not
immune to skew. **Shuffled hash join** builds local hash tables after a shuffle
when the planner considers it appropriate. Non-equi predicates may require
other algorithms, including expensive nested-loop strategies.

The course dimension has three rows, making its explicit broadcast reasonable.
For a changing production dimension, validate unique keys and size before
treating this hint as permanently safe.

## 1.5 Partition count, repartition, coalesce

There are different notions of partition:

* **Input partitions:** execution splits from files/source connectors.
* **Shuffle partitions:** redistribution buckets, initially influenced by
  `spark.sql.shuffle.partitions`; AQE can adjust eligible stages.
* **Directory partitions:** storage paths such as `event_date=2025-01-01/`.
  They are not a one-to-one mapping to executor tasks.

Too few tasks underutilize cores and create large working sets. Too many tiny
tasks waste scheduling and file-open overhead. Choose based on bytes per task,
skew, available cores, spill/GC, and downstream writes—not a magic number.
The course's four shuffle partitions are for tiny local fixtures only.

`repartition(n, "key")` shuffles to establish hash partitioning.
`coalesce(n)` usually reduces partitions with a narrow dependency, which can avoid
a shuffle but create imbalance and reduce upstream parallelism. It is not a
general method for increasing parallelism or fixing hot keys.
`coalesce(1)` can serialize a large output through one task. A global `orderBy`
can shuffle; `sortWithinPartitions` only gives per-partition order.

**Increasing partition count does not divide one equality key** across reducers
in a normal hash aggregate. A key always maps to one bucket at that stage.

## 1.6 Skew and correct salting

Evidence of skew: a long tail of tasks with much larger shuffle read/input,
spill, peak memory, or runtime than median tasks. Separate key skew from a
slow machine, uneven input files, expensive records, and data locality.
The skew fixture explicitly has 900 hot rows and ten cold keys with ten rows each.

Mitigation order:

1. Filter/project early when semantically valid.
2. Pre-aggregate before a join when the business grain permits it.
3. Broadcast a genuinely small side, eliminating a large-side join shuffle.
4. Inspect AQE skew handling for the join that actually runs.
5. Salt a diagnosed hot key only if added complexity and shuffle are justified.

For sum, define a stable salt from event ID:

```text
salt = hash(event_id) mod 8, for the hot key; otherwise 0
partial[key, salt] = sum(amount)
answer[key] = sum(partial[key, salt])
```

Use `pmod`, not `%` on signed hashes, so salt is nonnegative. Two-phase sum is
valid because partial sums can be merged. For average, carry **sum and count**,
then divide total sum by total count. Averaging bucket averages is wrong when
buckets have unequal counts. Exact median and exact distinct counts require
different merge state; do not sum partial distinct counts when values overlap.

For an equi-join, each fact gets exactly one salt; the matching hot dimension
row must be copied to **every** salt. Independently assigning one random salt
to each side loses matches. Replicating both fact and dimension rows can
multiply matches. Dimension uniqueness remains a precondition.

The lab intentionally allows Spark/AQE to choose algorithms. Its tiny dimension
may be broadcast, and the aggregate can benefit from map-side combine. Thus it
demonstrates correctness of salting, **not proof that a physical skew bottleneck
exists or that salting improves it**. Inspect the actual executed plan and stage
metrics before making a performance claim.

## 1.7 Cache versus checkpoint, and a measurement workflow

`persist` registers reuse; an action materializes the result. Cached partitions
may be evicted or lost and recomputed from lineage. A memory-and-disk level
can spill cached blocks to local disk; this is not durable distributed storage.
Always `unpersist` data no longer needed. Cache expensive, repeatedly reused
intermediates—not every DataFrame.

A reliable `checkpoint` writes materialized data to the configured checkpoint
filesystem and truncates lineage. Set a durable shared location for real
clusters. `localCheckpoint` uses executor-local storage and sacrifices reliable
recovery; executor loss may make the truncated lineage unrecoverable.
Neither is a database transaction or a replacement for a streaming checkpoint.

Measure in this sequence:

1. Record input bytes/rows, key distribution, schema, cluster size and settings.
2. Confirm result invariants before and after a proposed optimization.
3. Read physical/adaptive plans to verify the intended change actually happened.
4. Compare task distribution, shuffle read/write, spills, GC, and peak memory.
5. Separate cold reads from warm cache runs; keep data and resources constant.
6. Check total cost and tail latency, not only a driver-side stopwatch.

**Exercise checkpoint:** explain why caching the course's two-row result is
educational, why increasing partitions does not split the hot key, and why
salting can regress a sum that already combines efficiently on each mapper.
