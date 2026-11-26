# Spark zero-to-hero masterclass

This is the beginner-first route through the Spark track. The aim is not to
memorise DataFrame method names; it is to understand where data moves, when
work executes, which result contract is being protected, and how to measure a
performance claim.

The study loop is:

```text
understand -> run -> inspect -> change -> test -> explain
```

## 0. Version boundary and setup

The reproducible matrix is Spark/PySpark 3.5.6, Scala 2.12.18, JDK 17, and
CPython 3.10. Follow the complete setup in [COURSE.md](COURSE.md), then run the
Python tour:

```bash
cd 03-spark-distributed-computing
. .venv/bin/activate
export SPARK_LOCAL_IP=127.0.0.1
.venv/bin/spark-submit --master 'local[2]' pyspark/zero_to_hero.py
```

Build and run the Scala equivalent:

```bash
(cd scala && sbt -batch package)
JAR=scala/target/scala-2.12/spark-interview-course_2.12-1.0.0.jar
.venv/bin/spark-submit --master 'local[2]' \
  --class course.ZeroToHero "$JAR"
```

Both programs end with a `Zero-to-hero ... tour passed.` line. They create their
own small input, so no cloud account or external data is required.

## 1. Read the Python tour once from top to bottom

Open [`pyspark/zero_to_hero.py`](pyspark/zero_to_hero.py):

| Code | Spark idea | Interview explanation |
|---|---|---|
| `StructType` and `createDataFrame` | Explicit schema and typed input | Why is schema inference risky for production data? |
| `withColumn`, `filter`, `select` | Lazy, row-shaped transformations | Which operations have run before an action? |
| `broadcast` join | Replicate a small dimension | When does broadcast become a memory risk? |
| `Window` and `row_number` | Per-group calculation without collapsing rows | Why is the tie-breaker needed for deterministic output? |
| `cache` + `count` | Materialization and reuse | Why does calling `cache()` alone not execute work? |
| `explain("formatted")` | Logical/physical plan inspection | Where are the exchanges and expensive boundaries? |
| `repartition` | Partition layout, not executor count | Why does partition count differ from cluster size? |
| `collect`, `count`, `show` | Actions that execute a plan | Which action moves data to the driver? |

Use the following pass after each section:

1. Predict the schema and the row count.
2. Run the job and compare the printed report.
3. Read the physical plan and name every exchange.
4. Change one transformation and rerun the assertions.
5. Explain the changed behavior and its cost.

Suggested changes:

- Remove the explicit schema and describe how a malformed amount might be
  inferred.
- Change `topic == "java"` to a case-insensitive condition.
- Remove `order_id` from the window ordering and explain why equal timestamps
  make the chosen row non-deterministic.
- Replace `broadcast(customers)` with a normal join and compare the plan.
- Move `report.count()` before `report.cache()` and explain what was cached.
- Replace `collect()` with `show()` and explain the driver-memory difference.
- Set `spark.sql.shuffle.partitions` to `1` and explain why correctness should
  stay the same while parallelism changes.

## 2. The execution model to draw

For every job, draw this sequence:

```text
SparkSession
  -> logical plan
  -> optimized plan
  -> physical plan
  -> job
  -> stages separated by shuffle/exchange
  -> tasks operating on partitions
  -> action result or output
```

Classify each operation:

| Operation | Usually | Why |
|---|---|---|
| `select`, `filter`, `withColumn` | Narrow transformation | A child partition can be processed without matching another partition |
| `map`/`explode` | Narrow transformation | Rows are transformed locally |
| `groupBy`, `join`, `distinct`, `orderBy` | Often wide transformation | Records may need to move between partitions |
| `show`, `count`, `collect`, `write` | Action | The plan is submitted for execution |
| window functions | Depends on partition/order requirements | They commonly require repartitioning and sorting within groups |

Do not claim that every `join` shuffles: a broadcast join can avoid shuffling
the large side, and Adaptive Query Execution can change a plan at runtime.
Read the plan and measure.

## 3. Continue through the production-shaped labs

| Stage | Source | Mastery checkpoint |
|---|---|---|
| Execution | [`word_count.py`](pyspark/word_count.py) and execution docs | Explain tokenization, aggregation, and deterministic ordering |
| Correctness | [`sales_pipeline.py`](pyspark/sales_pipeline.py) and correctness docs | Reject malformed money, duplicate IDs, bad timestamps, and unknown keys |
| Performance | [`performance_plan.py`](pyspark/performance_plan.py) | Identify broadcast, aggregation, sort, cache, and exchange operators |
| Skew | [`skew_salting.py`](pyspark/skew_salting.py) | Prove salted output equals baseline output before discussing speed |
| Streaming | Streaming section in the docs | Explain event time, watermark, state, retries, and idempotent sinks |

Run the existing validation:

```bash
.venv/bin/python -m unittest discover -s pyspark/tests -v
(cd scala && sbt -batch package)
```

The local jobs prove correctness for their fixtures. They do not prove cluster
throughput, cloud storage behavior, executor sizing, or exactly-once external
side effects.

## 4. Revision card

```text
driver vs executor
job vs stage vs task
partition vs executor
transformation vs action
lazy plan vs materialized result
narrow vs wide dependency
shuffle/exchange boundary
broadcast join vs shuffle join
window partition/order vs groupBy collapse
cache declaration vs cache materialization
skew vs insufficient parallelism
task retry vs idempotent sink
event time vs processing time
watermark vs state cleanup
local success vs production benchmark
```

## 5. Completion checkpoint

Move to the Spring and messaging tracks when you can read a formatted plan,
predict whether an operation introduces a shuffle, explain why a broadcast
dimension is safe or unsafe, write a deterministic tie-breaker, and state what
must happen if a task retries after an external write.
