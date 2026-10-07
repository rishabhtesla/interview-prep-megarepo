# 4. Interview questions with substantive answers

Practice answering each in 60–90 seconds, then defend it with a lab or a failure
case. Avoid universal claims such as “broadcast is always faster” or “Spark
guarantees exactly once.”

## Execution and representations

### 1. What actually happens when you write a chain of DataFrame transformations?

Spark records a logical plan. Analysis resolves schemas/names/types, optimization
rewrites relational expressions, and physical planning chooses execution
operators. An action causes execution of the required work; metadata operations
can happen earlier. The driver schedules tasks and collects only requested
results/metadata. Executors perform partition work.

**Follow-up:** does `show(20)` scan only 20 rows? Not necessarily. A global
aggregate or sort may require processing the entire input to determine those
20 output rows. Use word count to illustrate the dependency.

### 2. Explain jobs, stages, tasks, and partitions without equating them.

A job is a scheduled unit of work associated with requested computation. Stages
group tasks that can execute without an intervening shuffle dependency. A task
processes a partition of a stage. A partition is a logical division of data,
not a machine. One executor runs many tasks over time.

An SQL action can cause auxiliary jobs for broadcasts or sampling, so “one
action = exactly one job” is too rigid. `local[2]` permits two concurrent task
threads; it is not a two-worker cluster.

### 3. What is narrow versus wide, and why is the distinction useful?

A narrow dependency allows pipelined consumption of a bounded set of parent
partitions, as in map/filter. A wide dependency redistributes data from many
parents, typically requiring shuffle. Shuffle introduces data transfer, local
files, memory pressure, and a stage boundary.

It tells you where data movement is required. It does not make operation names
absolute: a broadcast join need not shuffle the large side, and existing
partitioning can sometimes satisfy a requirement.

### 4. When would you use an RDD rather than a DataFrame?

Choose a DataFrame/SQL for relational data so Spark can reason about schemas,
expressions, column pruning, and execution strategies. An RDD can fit irregular
low-level transformations or algorithms requiring explicit partition-level
control, but optimization and serialization costs become your responsibility.

For counting words, `reduceByKey` is usually preferable to collecting all values
with `groupByKey`. The course uses DataFrame aggregation to expose partial/final
combination in a physical plan.

### 5. Is Scala Spark always faster than PySpark?

No. Both can build equivalent SQL plans executed by the JVM engine, so native
DataFrame expressions can have similar core execution characteristics. Python
worker/UDF/RDD code adds language-boundary and serialization considerations.
Scala UDFs avoid that boundary but can still obscure optimization.

Compare equivalent workloads and plans, not languages in isolation. The paired
labs deliberately use SQL built-ins in both languages.

## Planning, movement, and tuning

### 6. Distinguish Catalyst, Tungsten, and AQE.

Catalyst analyzes and optimizes relational plans and supports physical planning.
Tungsten describes execution improvements such as efficient binary memory
layouts and code generation. AQE uses runtime statistics to revise eligible
execution choices, including shuffle partition coalescing and some join changes.

They solve different problems. AQE does not rescue arbitrary unbounded UDF state
or split every single-key aggregation; generated code is not proof every
operator is fused.

### 7. What makes a broadcast join safe?

The broadcast side must fit within relevant driver/executor memory and timeout
budgets after serialization/deserialization, with concurrency considered. Its
join key/cardinality must also be correct. Good statistics help the planner;
an explicit hint bypasses some decision-making but does not manufacture memory.

A three-row dimension is safe in this lab. A fast-growing dimension needs a
different operational policy. Broadcast removes one large-side join shuffle,
not later aggregation or sort shuffles.

### 8. Why can a correct SQL join double revenue?

SQL can be mechanically correct while the assumed data grain is wrong. Duplicate
dimension keys create multiple matches per fact. Missing dimension keys drop
facts in an inner join. Null equality semantics can further change coverage.

Validate unique dimension keys and use a left-anti check for unknown references.
Compare pre/post join row counts and sums when a one-to-one/many-to-one
relationship is expected. See exercise 6.

### 9. A stage has one task running far longer than the others. What do you inspect?

Compare input/shuffle bytes, records, spill, GC, and executor health for the tail
task versus the median. A hot key is one possibility; another is a giant input
file, expensive record, slow disk, or unhealthy executor.

If skew is confirmed, consider filtering, pre-aggregation, broadcast, AQE join
skew handling, and finally selective salting. Raising all memory settings or
partition counts without evidence may waste resources without fixing the cause.

### 10. Why does increasing shuffle partitions not fix a single hot aggregate key?

Hash partitioning still assigns every occurrence of one key to the same bucket.
More buckets spread distinct keys, not one key's state. Map-side aggregation
can reduce transferred data, but a genuinely large per-key state remains a
problem.

Salting creates multiple intermediate keys and then merges their partial state.
That is valid only if the computation admits a correct merge operation.

### 11. Explain a correct salted join and a common incorrect one.

Assign each hot fact one deterministic salt and replicate the matching small
dimension row across all salt values. Join on key and salt. This spreads hot
matches without losing or multiplying facts, assuming unique dimension keys.

Independently assigning a random salt to both sides loses matches. Copying both
sides to all salts can multiply outputs. The skew lab compares complete
multisets, because equal row counts alone do not prove the join is correct.

### 12. How do you compute a salted average correctly?

For each `(key,salt)`, carry sum and count. Merge all sums and counts per key,
then divide. Never average averages without weighting by their counts.

The hot-key fixture has 900 rows totaling 3594, so its average is 3.993333… .
Exact median and exact distinct count require different state/algorithms;
ordinary two-stage sum logic cannot simply be copied.

### 13. Repartition or coalesce?

Use repartition when a shuffle is needed to establish desired partitioning or
increase/distribute parallelism. Coalesce commonly reduces partition count
without a full shuffle, but can preserve imbalance and limit parallelism.

Neither defines a universal ideal file count. `coalesce(1)` can turn a scalable
pipeline into one large output task. Base choices on task size, skew, downstream
layout, and measured overhead.

### 14. When should a DataFrame be cached, and when should it be checkpointed?

Cache expensive results reused by multiple actions when saved recomputation
outweighs memory/disk costs. Materialization requires an action. Eviction/loss can
cause recomputation; unpersist when reuse ends.

Reliable checkpointing writes durable data and truncates lineage, useful for
long iterative lineages. Local checkpointing trades durability for speed.
Neither is the same thing as a streaming query's offset/state checkpoint.

### 15. How would you prove a performance improvement?

First prove result equivalence and keep inputs/resources comparable. Inspect
initial and executed adaptive plans to verify the intended operator change.
Compare task tails, shuffle volume, spill, GC, and total resource/time cost.
Separate cold runs from cached runs.

The local plan lab is too small to establish cluster performance. A lower
driver stopwatch number alone can reflect caching or noise, not an optimization.

## Correctness and reliability

### 16. Why do nulls silently disappear from naive validation?

SQL uses three-valued logic. A predicate such as `amount > 0` is unknown for a
null amount. `filter(~valid)` also yields unknown for some invalid rows, and
filters retain only true. Therefore that validation can fail to detect nulls.

The course uses `~coalesce(valid, false)` to flag false and unknown as invalid.
Separately understand `count(*)`, `count(column)`, all-null sums, and null-safe
join equality before substituting default values.

### 17. Why use decimal and still validate the original money string?

Decimal avoids binary floating-point approximation for fixed-scale currency,
but a cast may round a value with excessive fractional digits. A string such
as `1.999` must fail this contract, not silently become `2.00`.

Validate the raw fractional-digit count, parse with an explicit precision/scale,
check overflow/null and allowed sign, and decide the overflow policy. These
rules model positive sales, not refunds; contracts should reflect the domain.

### 18. How do you select the latest event reliably?

Partition by the business entity and use `row_number` over descending event
time plus a deterministic unique tie-breaker. Filter to row number one. Compute
full-history metrics before discarding older events if those metrics are needed.

`dropDuplicates(entity)` picks an unspecified survivor; sorting beforehand is
not a durable guarantee across shuffles. Event time alone is not a total order
when events share timestamps.

### 19. What is the difference between ROWS and RANGE windows?

ROWS frames use row positions in the ordered partition. RANGE frames use
ordering values and can include all peers sharing an ordering value. With
timestamp ties, a default RANGE running sum can “jump” across multiple peers.

State the intended frame explicitly. The course's lifetime total uses the
entire customer partition; exercise 4 uses a deterministic ordered ROWS frame
for one-row-at-a-time running totals.

### 20. How do timestamp bugs appear even with valid parsed values?

Strings without offsets are interpreted in a session zone; date extraction and
display also depend on it. Changing zones can move a record across a daily
boundary. Ambiguous local daylight-saving times need additional source context.

The course specifies UTC and an exact string format. In a real contract,
distinguish instants from local wall-clock values, record zone/offset policy,
and test boundary times rather than trusting a successful parse.

### 21. Why are many tiny Parquet files a problem, and does partitionBy fix it?

Tiny files increase metadata/list/open and scheduling overhead. Directory
partitioning can help prune relevant data but can worsen small files when
cardinality is high. It is separate from Spark execution partitions.

Use adequate micro-batch/batch size, planned writer parallelism, and compaction.
Choose partition columns from real filter patterns and data volume. A plain
Parquet directory is not a transactional table or an upsert mechanism.

### 22. Spark retries failed tasks. Why can external writes still duplicate?

Multiple task attempts may execute the same code, including speculative attempts.
Spark can recompute data but cannot undo arbitrary email/API/database effects.
A driver can also fail after some sink transactions commit.

Use stable business/run keys and idempotent/transactional sink operations, with
commit recording tied atomically to publication where required. Task success
and end-to-end business exactly-once are different guarantees.

## Streaming boundaries

### 23. What does a watermark guarantee, and when is a window output final?

A watermark tracks event-time progress with a lateness delay. For supported
operations, data less late than the configured threshold is protected by the
documented guarantee; data beyond it may or may not be processed. It is not a
wall-clock timer or a blanket deduplication policy.

Append-mode window aggregates can emit final windows when the watermark passes
the relevant window end under operator rules. Idle inputs can delay progress.
Long delays and high key cardinality increase retained state. Update mode can
emit revisions earlier, requiring suitable downstream semantics.

### 24. Is a checkpoint enough for exactly-once Structured Streaming?

No. Checkpoints preserve source progress and state needed to recover a compatible
query. End-to-end exactly-once additionally depends on replayable sources and a
sink/commit protocol safe under reprocessing.

`foreachBatch` is at least once by default. Deduplicating by batch ID requires
query identity and an atomic sink transaction/commit record; a fresh checkpoint
can reset IDs. Deleting a checkpoint or changing stateful grouping is an
operational migration, not routine error handling. Console output is diagnostic,
not evidence of durable exactly-once publication.

## Closing mock interview

**Prompt:** “Design a daily customer-revenue pipeline with duplicate and late
events, a skewed customer, and a rerun-safe destination.”

A strong response asks about event IDs, grain, currency, time zone, accepted
lateness, data size, dimension history, and sink capabilities before choosing
Spark settings. It then specifies ingestion contracts, deduplication ordering,
join cardinality checks, measured skew mitigation, reconciliation metrics,
and publication/recovery semantics. Explain what remains unproven by the local
labs rather than presenting them as production benchmarks.
