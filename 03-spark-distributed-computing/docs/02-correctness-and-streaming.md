# 2. Correct data, resilient outputs, and streaming

## 2.1 Write a contract before a transformation

The course implements these rules in `clean_orders`/`cleanOrders`,
`clean_customers`/`cleanCustomers`, and `load_sales`/`loadSales`:

| Field / relation | Rule | Reason |
|---|---|---|
| CSV layout | Explicit ordered header and string schema; FAILFAST parsing | No schema inference or deliberate malformed-row dropping. |
| `order_id` | Required `o[0-9]+`, unique per input snapshot | Grain is one order. |
| `customer_id` | Required `c[0-9]+`; dimension key unique | Prevent missing and multiplied matches. |
| `amount` | Positive decimal(12,2); at most two fractional digits | Reject invalid text, overflow, negative/zero amounts, and silent rounding. |
| `event_time` | Exact `yyyy-MM-dd HH:mm:ss`, interpreted in UTC | Reject ambiguous/unparseable timestamps and implicit host-zone changes. |
| `region` | Exactly two uppercase ASCII letters | Shape check, not verification against a country registry. |
| Order → customer | Every order must match a known customer | An inner join must not silently lose revenue. |

The amount contract models **sales**, not returns/refunds. A refund feed needs a
different explicit sign/type contract; do not quietly reuse this validator.
Uniqueness is within this batch snapshot, not across every historical delivery.
Empty data passes the row-level checks; add a freshness/minimum-row rule when
the business requires nonempty daily deliveries.

Read raw strings first. `try_cast` and `try_to_timestamp` expose parse failure as
null for validation even with ANSI mode enabled. They are **not** used to make
bad data disappear. A composed predicate is coalesced to `false`, so null fields
are invalid instead of disappearing under SQL three-valued logic.

Decimal casting may round excess fractional digits; therefore a successful
decimal cast alone is insufficient. The raw-string precision rule rejects
`1.999` before accepting its potentially rounded cast. The timestamp round-trip
check rejects text that does not follow the exact specified representation.

CSV FAILFAST is not a universal byte-level file contract: CSV parser behavior
around extra/missing tokens and column pruning has subtleties. The explicit
header and semantic checks reject required missing values and domain errors.
If every raw row must have an exact field count or signature, enforce that in
a dedicated ingestion layer; these labs do not claim forensic CSV validation.

### Fail or quarantine?

The labs **abort the job** when invalid data is observed. They print a reason,
not the full offending record, to avoid leaking source data. There is no catch
that returns an empty DataFrame or changes an error into a successful report.

For production quarantine, retain immutable source identity, ingestion time,
rule/version, reason, and authorized raw payload in a restricted reject store.
Report accepted/rejected/total counts and enforce a rejection threshold. The
business owner decides whether a partial report is publishable. Merely filtering
bad records and saying “success” is not a quality policy.

Validation here triggers several actions and subsequent reports can reread
inputs. Files **must remain immutable during the job**. On large datasets,
validate one immutable/versioned snapshot and persist/materialize a clean layer
before downstream use. Cache alone does not protect against mutable inputs when
lost partitions are recomputed.

## 2.2 Null semantics, types, and deterministic calculations

SQL null is unknown, not zero or an empty string:

* `NULL = NULL` is unknown, not true; use `isNull` or null-safe equality (`<=>`,
  PySpark `eqNullSafe`) only when that expresses the business rule.
* A filter keeps only true rows. False **and unknown** rows are dropped.
* `count(*)` counts rows; `count(column)` skips nulls.
* `sum` ignores null operands, but an all-null group returns null.
* A normal equi-join does not match two null keys. Null-safe matching can change
  cardinality drastically when both sides have many nulls.

Do not `fillna(0)` for unknown money without deciding that unknown really means
zero. Otherwise reported revenue becomes an unsupported estimate.

Use decimals for currency where exact base-10 arithmetic matters. Spark can
widen the result precision for decimal sums, subject to a maximum precision.
With ANSI enabled, arithmetic overflow should fail rather than quietly produce
a misleading result. Floating point addition is not associative; different
partition/reduction orders can give small differences in floating-point sums.
Exact decimal/long assertions in these labs avoid that issue.

DataFrame schemas describe types but do not replace domain rules. A nullable
field is a representation option, not permission for missing business keys.
Avoid schema inference for evolving production feeds; deliberately version
contracts and test compatible/incompatible changes.

## 2.3 Event time, zones, and windows

`event_time` is when something happened; processing/ingestion time is when the
system observed it. Batch timestamps are parsed with the session zone **UTC**.
Spark `TimestampType` represents an instant at microsecond resolution; rendering,
parsing strings without offsets, and date extraction depend on the session zone.
`timestamp_ntz` represents local wall-clock time without a zone and has different
semantics. Choose deliberately rather than mixing them.

Inputs here have no offset because the contract says they are UTC. Real feeds
with local time need an explicit IANA zone or an offset; daylight-saving gaps and
repeated local times cannot always be resolved from a timestamp string alone.
Do not apply `to_utc_timestamp` to an already correctly parsed UTC instant just
because the function sounds useful—it can shift an instant incorrectly.

SQL analytical windows retain rows:

```python
from pyspark.sql import Window, functions as F
by_customer = Window.partitionBy("customer_id")
latest = by_customer.orderBy(F.desc("event_ts"), F.desc("order_id"))

snapshot = (orders
    .withColumn("total", F.sum("amount").over(by_customer))
    .withColumn("rn", F.row_number().over(latest))
    .filter("rn = 1"))
```

The unordered aggregate window covers the customer's entire partition. A
running total needs an ordered window and an explicit frame:

```python
running = (Window.partitionBy("customer_id")
           .orderBy("event_ts", "order_id")
           .rowsBetween(Window.unboundedPreceding, Window.currentRow))
orders.withColumn("running_total", F.sum("amount").over(running)).show()
```

`ROWS` counts physical row positions within the specified ordering; `RANGE`
groups relative ordering values/peers. Default frames can surprise you when
multiple rows share the same timestamp. `row_number`, `rank`, and `dense_rank`
also differ: ties get distinct positions, gaps in rank, and no gaps respectively.
For “one latest record,” define a deterministic total ordering and use
`row_number`; `dropDuplicates(["customer_id"])` does not mean “keep newest.”

## 2.4 Storage layout and the small-file problem

CSV is useful for inspectable fixtures, but is row-oriented text with parsing
cost and limited typing. Parquet preserves types and supports column pruning,
statistics-based row-group skipping, and compression. Filter pushdown is not the
same as partition pruning or a promise that no bytes will be read.

For sufficiently large daily data, partition directories by a low-to-moderate
cardinality date used by common filters:

```python
from pyspark.sql import functions as F
daily = orders.withColumn("event_date", F.to_date("event_ts"))
(daily.repartition(4, "event_date")
 .write.mode("errorifexists").partitionBy("event_date")
 .parquet(".runtime/orders-by-date"))
```

This is an exercise snippet after loading `orders`, not a required lab side
effect. Rerunning it should fail until you choose a new output path or explicitly
manage the existing one. The two tiny sample dates do not justify this storage
layout at production scale; they only make it visible.

Directory partitioning on high-cardinality order/customer IDs explodes directory
and small-file counts. Conversely, a single date with huge traffic can still
need many files. `repartition(4, "event_date")` is not “four files per date.”
Writer tasks and partition values determine files; rollover settings such as
`maxRecordsPerFile` can create more.

Small files cost metadata listing/open work, scheduling, and transaction-log
overhead in table formats. Periodic compaction, sensible batching, and tuned
writer parallelism are preferable to global `coalesce(1)`. Choose file-size
targets based on storage and workload; no byte size is universally correct.

Plain Parquet files do not provide a table transaction log, atomic multi-file
updates, time travel, or upserts. Table formats such as Delta/Iceberg/Hudi add
protocols for some of these concerns but are **not installed in this course**.
Do not call a plain `overwrite` a transactional merge.

## 2.5 Fault tolerance, retries, and publishing safely

RDD lineage and DataFrame plans allow recomputation after partition loss.
Spark retries failed tasks according to configured policies. Lost shuffle
outputs may force earlier stages to rerun. Speculative execution may run
duplicate task attempts to mitigate stragglers. None of this makes arbitrary
external side effects safe.

An executor loop that charges a credit card, sends an email, or inserts rows
without a uniqueness key can duplicate effects on retries. Avoid non-idempotent
side effects in transformations. Do not use accumulator values as a financial
source of truth; retries/re-execution and how updates are made matter.

A batch publication pattern:

1. Identify an immutable input manifest or version and a stable logical run ID.
2. Write to a run-specific staging/output location.
3. Validate row counts, totals, uniqueness, and expected partition coverage.
4. Publish using the destination's transactional commit mechanism, or a
   carefully designed atomic metadata pointer where supported.
5. Record the committed input/run identity; reruns should no-op or replace the
   same logical result under a safe concurrency protocol.

File committer protocols coordinate task output, but atomicity depends on the
filesystem/object store and connector. Rename assumptions from a local
filesystem may not apply to object storage. Concurrent writers and driver
failure during publication need explicit handling.

JDBC retries are not made exactly once simply by using Spark's JDBC writer.
Use database uniqueness/upsert/transaction semantics with stable keys. A failed
application can have partially committed external writes; reconcile before
retrying. Idempotency means “repeating this logical operation produces the same
observable state,” not “the framework tries to execute it once.”

## 2.6 Structured Streaming: a changing table, not a loop around batch

A streaming query incrementally updates a logical result table. In default
micro-batch execution, Spark processes available input offsets/files in batches,
updates state if necessary, and commits progress. Transformations remain lazy
until `writeStream.start()` launches the query.

Three output modes have different meanings:

* **Append:** emit newly final rows supported by the query. A windowed aggregate
  generally needs a watermark to know when a window can be final.
* **Update:** emit changed rows each trigger; the consumer needs update/upsert
  semantics if it maintains one current row per key.
* **Complete:** emit the entire aggregate result each trigger; retaining the
  entire result can make state grow. It is not a scalable default for unbounded
  key cardinality.

Support depends on the exact query and sink. Console and memory sinks are useful
for development, not durable production publication mechanisms.

### A runnable rate-source demonstration (optional)

After the batch labs, run this in `.venv/bin/pyspark --master 'local[2]'`.
It uses no external service or fixture, and writes only its local checkpoint.
Stop it after inspecting several triggers. The rate source synthesizes timestamps;
it does **not** demonstrate out-of-order business events or historical replay.

```python
from pyspark.sql import functions as F
spark.conf.set("spark.sql.session.timeZone", "UTC")
spark.conf.set("spark.sql.shuffle.partitions", "2")
events = (spark.readStream.format("rate")
          .option("rowsPerSecond", 2).option("numPartitions", 1).load())
counts = (events.withWatermark("timestamp", "10 seconds")
          .groupBy(F.window("timestamp", "10 seconds")).count())
query = (counts.writeStream.outputMode("update").format("console")
         .option("truncate", "false")
         .option("checkpointLocation", ".runtime/rate-checkpoint")
         .trigger(processingTime="5 seconds").start())
try:
    query.awaitTermination(30)
finally:
    query.stop()
```

Wall-clock-dependent counts are not deterministic expected-output fixtures.
The snippet illustrates API placement and state/output concepts; it is not part
of the batch test suite. Preserve a checkpoint only when restarting a compatible
query; for a deliberately different demo query choose a new checkpoint path.

### Watermarks: what they do and do not promise

A watermark is based on observed event-time progress and a configured delay,
not wall-clock elapsed time. For a single input it is conceptually
`max_observed_event_time - delay`, with propagation across triggers. If no newer
event-time data arrives, a window may not close merely because the clock advances.
Multi-input queries have additional global-watermark policy considerations;
the slowest input normally constrains safe progress.

For a supported window aggregation, place `withWatermark` on the event-time
column **before** aggregation and aggregate on that column/window so Spark can
use it for state cleanup. Records less late than the configured threshold are
protected by the documented guarantee; records later than the threshold **may
or may not be processed**. Do not promise a precise universal drop boundary for
every trigger or operator. A watermark is not a sort, a schedule, or an SLA.

For a ten-minute window and a ten-minute delay, the state remains until the
watermark has passed that window's end under the operator's rules. In append
mode, final output is delayed accordingly. Update mode can emit partial changes
earlier. High-cardinality keys and long lateness windows increase state size.

Watermarks do not generally deduplicate events automatically. Spark 3.5 provides
`dropDuplicatesWithinWatermark` for bounded deduplication with a watermark:
choose stable event IDs and an appropriate delay. Its guarantee is bounded;
duplicates arriving after retained state is removed are not covered as an
all-time uniqueness guarantee.

### Checkpoints and exactly-once boundaries

A streaming checkpoint stores progress metadata, offsets/commit information,
and state-store data as required by the query. Use one durable location per
query in production, never a shared directory between independent queries.
Do not delete it to “fix” a failed production query without a replay and duplicate
strategy. Stateful schema/grouping changes are often incompatible with an
existing checkpoint; consult the supported restart-change rules.

Exactly-once end-to-end results require replayable input plus a sink and commit
protocol that can handle replay safely. Engine fault tolerance alone is not
enough. Kafka output and arbitrary external effects can be at least once.
`foreachBatch` is at least once by default; `(query identity, batchId)` can be
used to deduplicate sink transactions if the sink atomically records completion
with its writes. A new checkpoint can reset batch numbering, so `batchId` is not
a universal business-event ID.

Stream-stream joins need explicit event-time bounds and suitable watermarks to
bound state; outer join null results can be delayed until Spark knows a match
will no longer arrive. A static-dimension join has different state behavior and
is often simpler. Not every batch window/join combination is supported on streams.

**Interview checkpoint:** if a query has a watermark and checkpoint, can it still
duplicate a payment? Yes. Those features do not supply the payment service's
idempotency/transaction protocol.
