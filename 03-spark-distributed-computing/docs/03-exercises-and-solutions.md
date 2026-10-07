# 3. Exercises with worked solutions

Attempt the requirement and state an invariant before reading the solution.
“It ran” is not enough: check grain, totals, duplicate handling, and determinism.

## Working environment

Use the README's Java/Python environment and start from the track directory.
For interactive PySpark:

```bash
.venv/bin/pyspark --master 'local[2]'
```

```python
import sys
sys.path.insert(0, "pyspark")
from course import load_sales, clean_orders, read_strings, ORDER_COLUMNS
from pyspark.sql import functions as F, Window
spark.conf.set("spark.sql.session.timeZone", "UTC")
spark.conf.set("spark.sql.ansi.enabled", "true")
spark.conf.set("spark.sql.legacy.timeParserPolicy", "CORRECTED")
spark.conf.set("spark.sql.shuffle.partitions", "4")
orders, customers = load_sales(spark, "data")
orders.createOrReplaceTempView("orders")
customers.createOrReplaceTempView("customers")
```

The SQL solutions also run in Scala using the jar built in the README:

```bash
.venv/bin/spark-shell --master 'local[2]' \
  --jars scala/target/scala-2.12/spark-interview-course_2.12-1.0.0.jar
```

```scala
import course.Course
spark.conf.set("spark.sql.session.timeZone", "UTC")
spark.conf.set("spark.sql.ansi.enabled", "true")
spark.conf.set("spark.sql.legacy.timeParserPolicy", "CORRECTED")
val (orders, customers) = Course.loadSales(spark, "data")
orders.createOrReplaceTempView("orders")
customers.createOrReplaceTempView("customers")
```

Use `spark.sql("""...""").show()` in either shell for the SQL below. Quit shells
after use; they retain a live Spark context and local UI.

## Exercise 1 — Tokenization is a data contract

**Task:** predict counts for punctuation, mixed case, and empty tokens. Extend
the input with `"SPARK...spark"` without changing the tokenizer.

**Worked solution:** `course.word_counts` and `Course.wordCounts`, called by the
word-count entrypoints. The original fixture yields spark=3, parallel=2, and
five singleton words. The extra line increments spark to 5; no punctuation or
empty word should be counted. In Python:

```python
from course import word_counts
lines = spark.read.text("data/lines.txt")
extra = spark.createDataFrame([("SPARK...spark",)], ["value"])
assert word_counts(lines.unionByName(extra)).first()["count"] == 5
```

**Explain:** `[a-z]` is a deliberately limited domain. For multilingual content,
agree on Unicode normalization, token boundaries, and case folding first.
The test `test_word_count_has_stable_tie_order_and_no_empty_word` covers baseline
counts and tie ordering.

## Exercise 2 — Daily regional revenue without losing money

**Task:** produce one row per `(UTC event date, region)` with order count and
revenue. Prove revenue conservation.

**Worked solution:**

```sql
SELECT to_date(o.event_ts) AS event_date, c.region,
       count(*) AS orders, sum(o.amount) AS revenue
FROM orders o JOIN customers c ON o.customer_id = c.customer_id
GROUP BY to_date(o.event_ts), c.region
ORDER BY event_date, region
```

```text
2025-01-01  GB  1  15.00
2025-01-01  US  2  30.00
2025-01-02  GB  1  25.00
2025-01-02  US  2  15.00
```

Counts sum to 6; revenue sums to 85.00, equal to the validated fact table.
This conservation is valid because `load_sales` verifies a unique dimension
key and complete references. Without those preconditions, an inner join can
drop orders or multiply revenue. See `region_totals` / `Course.regionTotals`
for the runnable non-daily version.

## Exercise 3 — Latest row plus full history

**Task:** return each customer's latest order and lifetime revenue. Include a
same-timestamp tie. Explain why sorting then `dropDuplicates` is insufficient.

**Worked solution:** the sales-pipeline entrypoints already implement this.
Equivalent SQL:

```sql
WITH ranked AS (
  SELECT o.*, c.region,
         sum(amount) OVER (PARTITION BY o.customer_id) AS lifetime_total,
         row_number() OVER (
           PARTITION BY o.customer_id ORDER BY event_ts DESC, order_id DESC
         ) AS rn
  FROM orders o JOIN customers c ON o.customer_id = c.customer_id
)
SELECT customer_id, region, order_id, amount, lifetime_total
FROM ranked WHERE rn = 1 ORDER BY customer_id
```

Expected order IDs: o2, o5, o6; totals: 30.00, 40.00, 15.00.
Adding o9/c1/1.00 at `2025-01-01 11:00:00` selects o9 and gives total 31.00.
The tie-breaker is lexical string order, not a numerical sequence assumption.
Global ordering before a later exchange does not guarantee which record
deduplication retains. See `test_window_tie_breaker_is_deterministic`.

## Exercise 4 — Running revenue and frame semantics

**Task:** compute a running total per customer in ascending event-time order,
with a deterministic order ID tie-breaker.

**Worked solution:**

```sql
SELECT customer_id, order_id, amount,
       sum(amount) OVER (
         PARTITION BY customer_id ORDER BY event_ts, order_id
         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
       ) AS running_total
FROM orders ORDER BY customer_id, event_ts, order_id
```

Expected `(order_id, running_total)`:
`(o1,10.00), (o2,30.00), (o3,15.00), (o5,40.00), (o4,5.00), (o6,15.00)`.

`ROWS` makes accumulation happen one ordered row at a time. A `RANGE` frame
ordered only by event time can give all same-time peers the same accumulated
total. The full-history window in exercise 3 intentionally has no running frame.

## Exercise 5 — Fail loudly instead of “cleaning” away errors

**Task:** accept a valid `12.30` order; reject null IDs, negative/zero/invalid money,
`1.999`, overflow, impossible dates, and duplicate order IDs.

**Worked solution:** `clean_orders` / `Course.cleanOrders` combine raw-string
rules with explicit parsing. Run the deliberate bad fixture:

```python
bad = read_strings(spark, "data/invalid_orders.csv", ORDER_COLUMNS)
try:
    clean_orders(bad)
    raise AssertionError("The invalid fixture was accepted")
except ValueError as error:
    assert "Invalid order" in str(error)
```

The unittest suite tests each invalid category separately, so one bad field
cannot mask a missing check on another. Why not `filter(amount.isNotNull())`?
It silently drops rejected money; why not simply cast? Decimal casts can round
excess fractional digits. `coalesce(valid, false)` ensures null validity
predicates are rejected.

**Extension:** design a reject table and quality dashboard. Record rule ID and
source identity, accepted/rejected counts, and a publication threshold. This is
a design exercise, not implemented persistence.

## Exercise 6 — Prove join cardinality

**Task:** show that a duplicate c1 dimension row changes the result even though
the join succeeds. Show that missing c1 loses two orders in an inner join.

**Worked solution (use validated inputs, then intentionally damage the dimension):**

```python
duplicated = customers.unionByName(customers.filter("customer_id = 'c1'"))
assert orders.join(duplicated, "customer_id").count() == 8
missing = customers.filter("customer_id != 'c1'")
assert orders.join(missing, "customer_id").count() == 4
assert orders.join(missing, "customer_id", "left_anti").count() == 2
```

Production code must call `clean_customers`/`load_sales` before these joins.
Use a semi join for “customers with an order” when right-side columns are
unneeded; a semi join does not duplicate left rows for multiple right matches.
Tests `test_customer_quality_and_uniqueness` and
`test_unknown_customer_is_not_silently_lost` exercise the guardrails.

## Exercise 7 — Salt sum correctly; do not average averages

**Task:** recover unsalted totals from the hot-key fixture; then compute average
amount without biasing small salt buckets.

**Worked solution:** the skew entrypoints assert sums and join equality. For an
average, carry both sufficient statistics:

```python
from course import skew_facts
facts = skew_facts(spark)
salted = facts.withColumn(
    "salt",
    F.when(F.col("key") == "hot", F.pmod(F.xxhash64("event_id"), F.lit(8)))
     .otherwise(F.lit(0)))
parts = salted.groupBy("key", "salt").agg(
    F.sum("amount").alias("s"), F.count("*").alias("n"))
answer = parts.groupBy("key").agg(
    F.sum("s").alias("s"), F.sum("n").alias("n"))
answer = answer.select("key", (F.col("s") / F.col("n")).alias("average"))
assert abs(answer.filter("key = 'hot'").first().average - 3.993333333333333) < 1e-12
```

The hot key has sum 3594 and count 900. If bucket A has `[1]` and bucket B has
`[9,9,9]`, averaging bucket averages gives 5, but the correct average is 7.
Choose a numerical tolerance for floating-point division; money sums remain
exact decimals in the sales pipeline.

## Exercise 8 — Explain before optimizing

**Task:** identify the joins/exchanges in the performance lab, then remove the
explicit broadcast hint and compare plans. Do not claim improvement from one
local timing.

**Worked solution:**

```python
plain = orders.join(customers, "customer_id").groupBy("region").agg(F.sum("amount"))
plain.explain("formatted")
plain.collect()  # safe only for this two-row fixture
plain.explain("formatted")
```

Spark may still broadcast automatically. To study a different join, temporarily
disable automatic and adaptive broadcasting in the interactive experiment, or
use a supported merge hint, then **verify** the physical plan. Configuration
changes are not proof a particular algorithm executed. Restore defaults or
restart the shell afterward.

The worked baseline is `performance_plan.py` / `PerformancePlan.scala`. Their
cache demonstrates materialization/unpersist, not a performance recommendation.
At scale measure shuffle bytes, spill, GC, task tail, and result equivalence.

## Exercise 9 — Read storage partitions without confusing task partitions

**Task:** write orders as date-partitioned Parquet, then read only January 2.
Predict count and revenue before running.

**Worked solution:**

```python
output = ".runtime/exercise-orders"
(orders.withColumn("event_date", F.to_date("event_ts"))
 .write.mode("errorifexists").partitionBy("event_date").parquet(output))
second_day = spark.read.parquet(output).filter("event_date = DATE '2025-01-02'")
second_day.explain("formatted")
assert second_day.count() == 3
assert str(second_day.agg(F.sum("amount")).first()[0]) == "40.00"
```

Look for a date `PartitionFilters` entry in the scan. File count is not promised
by this exercise. On rerun, use a new output path or consciously clean up your
own exercise output; do not replace `errorifexists` with a blind production
overwrite. Discuss compaction before suggesting `coalesce(1)`.

## Exercise 10 — Design a rerun-safe publication

**Task:** a job writes half its external database rows and then loses its driver.
What does Spark retry guarantee, and how should the next run behave?

**Worked design:** Spark can rerun computation, not roll back arbitrary database
transactions. Use stable business keys and a run/input version. Write to staging,
validate, and have the database atomically merge/publish with a commit record,
or use per-record idempotent upserts when their concurrency/version semantics
fit the business. If the commit record and data writes are separate independent
transactions, a crash between them still permits duplication or lost effects.
See [publishing safely](02-correctness-and-streaming.md#25-fault-tolerance-retries-and-publishing-safely).
No external sink is installed or simulated by the batch checks.

## Exercise 11 — A watermark is not a timer

**Task:** with ten-minute windows and a ten-minute watermark delay, discuss
events at 12:01, 12:04, and 12:25, followed by a late 12:03 event. Then input
goes idle. When can the 12:00–12:10 result become final?

**Worked design:** observation of event-time 12:25 can advance the conceptual
watermark toward 12:15, once that progress is propagated to an eligible trigger.
The 12:00–12:10 window can then be finalized/evicted under the operator's rules.
The subsequent 12:03 event is outside the delay relative to observed progress;
it is not guaranteed to be accepted. Do not claim every such record is dropped
at exactly one trigger boundary. If the input never advanced beyond 12:04,
waiting in wall-clock time alone would not move the watermark to 12:15.
Output mode, trigger boundaries, and multiple inputs affect the details.

Use the optional rate-source demonstration to inspect trigger output, but use
a controlled file/Kafka test source to test genuine late arrivals.

## Exercise 12 — Explain a checkpoint restart failure

**Task:** a streaming query is restarted with different grouping keys against its
old checkpoint, or with its checkpoint deleted. Why are both risky?

**Worked design:** state schema/operator compatibility can prevent the changed
query from reading old state. Deleting the checkpoint loses progress/state and
can replay or skip input depending on source configuration; it is not a general
repair. Choose a migration/backfill plan, a new query identity/checkpoint when
appropriate, and idempotent sink semantics. Reusing only `batchId` after a new
checkpoint is unsafe because numbering may restart.

## Self-assessment rubric

For each exercise, score 0–2 on: correct result, explicit grain/type assumptions,
failure handling, distributed-execution explanation, and validation strategy.
A working snippet without a correctness explanation is at most 4/10. A
performance proposal without metrics and output equivalence is incomplete.
