# Spark, Flink, Akka, and distributed data processing

## Mental model

Distributed processing is about data movement, state ownership, retries, and
output contracts. API syntax is secondary.

```text
input
  -> schema + validation
  -> lazy transformations
  -> partition/shuffle boundary
  -> task execution on executors
  -> state/window/checkpoint (streaming)
  -> deterministic output
```

## Run the Spark course

Follow the pinned matrix in `03-spark-distributed-computing/COURSE.md`:

```bash
cd 03-spark-distributed-computing
python3.10 -m venv .venv
. .venv/bin/activate
python -m pip install -r requirements.txt
.venv/bin/python -m unittest discover -s pyspark/tests -v
.venv/bin/spark-submit --master 'local[2]' pyspark/zero_to_hero.py
```

Then build/run the Scala 2.12 jar using the commands in that course guide.
Compare `pyspark/zero_to_hero.py` with
`scala/src/main/scala/ZeroToHero.scala`.

## Spark interview questions and answers

### Transformation versus action?

**Answer:** Transformations build a lazy logical plan; actions request a result
and trigger execution. `select`, `filter`, and `groupBy` describe work;
`count`, `collect`, `show`, and writes execute it. Laziness allows Spark to
optimize a plan but can surprise someone who expects each line to run
immediately.

### Narrow versus wide dependency?

**Answer:** A narrow dependency reads data from one parent partition, so it can
be pipelined in one stage. A wide dependency requires data redistribution,
typically a shuffle, such as `groupBy`, `join`, `distinct`, or `orderBy`.
Shuffles cost network I/O, serialization, disk spill, and coordination.

### Broadcast join versus shuffle join?

**Answer:** Broadcast sends a small dimension to executors so the large side
does not shuffle. It is effective only when the broadcast fits safely in
executor memory. A shuffle join redistributes both sides by key and is more
general but more expensive. Measure the physical plan; do not broadcast a
large table because a local test passed.

### Why is data skew different from too few partitions?

**Answer:** Too few partitions limits parallelism generally. Skew sends a
disproportionate amount of data for one key to one task, so most tasks finish
while one straggler remains. Salting can split a hot key into bounded buckets,
but it adds a second aggregation/join step and can be slower on small data.

### What do cache and checkpoint mean?

**Answer:** Cache/persist keeps a computed dataset for reuse after an action;
it is an optimization and has an eviction/storage-level cost. A checkpoint
materializes a reliable lineage boundary, especially for long streaming
lineages or recovery. Neither makes external side effects exactly once.

## Flink questions

### Event time, processing time, watermark, and state?

**Answer:** Event time is when the event occurred; processing time is when the
job observed it. A watermark is a progress estimate for event-time completeness.
State retains information across records/windows. A checkpoint snapshots
recoverable operator state. Late data needs an explicit allowed-lateness and
update/retraction policy.

```text
event timestamp -> keyed state/window -> watermark passes end
                -> emit/update result -> checkpoint state
```

Flink SQL's windowing shape is:

```sql
SELECT customer_id, window_start, COUNT(*) AS order_count
FROM TABLE(
  TUMBLE(TABLE orders, DESCRIPTOR(event_time), INTERVAL '5' MINUTES)
)
GROUP BY customer_id, window_start, window_end;
```

## Akka questions

### Why use actors and cluster sharding?

**Answer:** An actor owns state and processes messages sequentially for its
mailbox, making per-key state transitions easier to reason about. Cluster
sharding locates entities across nodes and moves ownership during topology
changes. It does not make arbitrary external side effects atomic; persistence,
duplicate messages, retries, passivation, and recovery still need a policy.

### How would you deduplicate incidents?

**Answer:** Define event identity, merge predicate, retention/deduplication
window, persisted state, and recovery behavior. Route events by incident key so
one owner serializes updates. Persist the identity/state before claiming the
event is processed, and make downstream publication idempotent.

## Code and configuration walkthrough

| Concern | File/config |
|---|---|
| Schema, joins, windows, caching | `03-spark-distributed-computing/pyspark/zero_to_hero.py` |
| Physical plan | `pyspark/performance_plan.py` |
| Skew/salting | `pyspark/skew_salting.py` |
| Python Spark tests | `pyspark/tests/` |
| Scala binary compatibility | `scala/build.sbt` |
| Event/state simulation | `09-messaging-streaming/src/BrokerSemantics.java` |

Spark settings such as executor memory, shuffle partitions, checkpoint
location, and output mode must be tied to data volume and failure semantics.
`local[2]` means two local task threads, not two production executors.

## Failure exercise

Use the invalid CSV fixture. Confirm that the pipeline fails on bad/missing
amounts, duplicate IDs, or unknown customers rather than silently dropping
rows. Then inspect a physical plan and point to every exchange. The question is
not “does the job finish?” but “what data moved and what result contract was
protected?”

## Revision card

```text
Spark transformations are lazy; actions execute the plan.
Wide dependencies create shuffle boundaries.
Broadcast only data that safely fits executor memory.
Skew is a hot-key distribution problem, not merely a partition-count problem.
Flink state/time/checkpoints define streaming correctness.
Actors serialize per-owner state; sharding does not remove distributed failure.
Retries require idempotent output and explicit checkpoint/recovery behavior.
```
