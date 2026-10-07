# Spark and data engineering: from plans to trustworthy pipelines

An interview course with **four equivalent batch labs in Scala and PySpark**,
bundled inputs, deliberate bad data, executable assertions, and worked exercises.
No cluster, cloud account, database, or external dataset is needed. Internet access
is needed once to obtain the build/runtime dependencies.

## Course map

| Session | Read / do | Completion criterion |
|---|---|---|
| 1 — Execution (90 min) | [Execution model](docs/01-execution-and-performance.md), word count | Draw a logical DAG; identify exchanges, task boundaries, and actions. |
| 2 — Correctness (2 h) | [Data engineering](docs/02-correctness-and-streaming.md), sales pipeline | Explain decimal/null/time contracts and prove join cardinality. |
| 3 — Performance (2 h) | Performance-plan and skew labs | Read a physical plan; distinguish hot keys from too few partitions. |
| 4 — Reliability (90 min) | Fault tolerance, output contracts, streaming section | Explain why task retries do not make external side effects exactly once. |
| 5 — Practice (2–3 h) | [Exercises and worked solutions](docs/03-exercises-and-solutions.md) | Change a transformation, predict output, and validate the invariant. |
| 6 — Mock interview (60 min) | [24 interview questions](docs/04-interview-questions.md) | Give a mechanism, tradeoff, failure mode, and measurement for each answer. |

Prerequisites: SQL grouping/joins, basic Python or Scala, and the distinction between
process memory, disk, and a network. Follow either language first; compare both
implementations to separate Spark semantics from language syntax.

## Compatible toolchain — do not mix binary versions

| Component | Course version | Why |
|---|---|---|
| Apache Spark / PySpark | **3.5.6** | Same SQL engine and APIs in both languages; intentionally pinned, not a claim of latest release. |
| Scala | **2.12.18**, artifact suffix **`_2.12`** | Matches the Scala 2.12 Spark distribution installed by this PySpark version. Scala 3 source syntax is not used. |
| JDK | **17** | Supported by Spark 3.5; the machine's default JDK may be too new. |
| CPython | **3.10.x** | The reproducible course interpreter; Python 3.12+ is not this course's supported baseline. |
| sbt | **1.10.7** | Pinned in `scala/project/build.properties`. |

Spark supports other combinations; these examples commit to the matrix above.
Use a current vendor-maintained Java 17 patch in your environment. Dependency
pinning enables reproducibility, not a security support guarantee.

### 1. Prepare the shell (start in this directory)

```bash
cd interview-prep-megarepo/03-spark-distributed-computing

# macOS: select an installed JDK 17, not the default JDK.
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
# Linux alternative: export JAVA_HOME=/absolute/path/to/your/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
java -version
python3.10 --version

mkdir -p .runtime/work .runtime/spark
export TMPDIR="$PWD/.runtime/work"
export SPARK_LOCAL_DIRS="$PWD/.runtime/spark"
export JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=$PWD/.runtime/work"
export SPARK_LOCAL_IP=127.0.0.1

python3.10 -m venv .venv
. .venv/bin/activate
python -m pip install -r requirements.txt
export PYSPARK_PYTHON="$PWD/.venv/bin/python"
export PYSPARK_DRIVER_PYTHON="$PYSPARK_PYTHON"
.venv/bin/spark-submit --version
```

The explicit `.venv/bin/spark-submit` avoids accidentally using a globally installed
Spark 4 or a Scala 2.13 distribution. The PySpark wheel includes a usable Spark
distribution and its JVM jars: it can submit **both Python scripts and Scala jars**.
If using a separately downloaded distribution, it must be Spark **3.5.6 built for
Scala 2.12**, with `JAVA_HOME` still set to Java 17. Do not submit this jar to a
Scala 2.13 runtime. `local[2]` means two concurrent task threads, **not two executors**.

Local data paths work only in local mode. For a real cluster, distribute code and
put the input on storage accessible to every executor. Do not copy these localhost
settings into a cluster deployment.

### 2. Run the four Python labs

```bash
.venv/bin/spark-submit --master 'local[2]' pyspark/word_count.py --data-dir data
.venv/bin/spark-submit --master 'local[2]' pyspark/sales_pipeline.py --data-dir data
.venv/bin/spark-submit --master 'local[2]' pyspark/performance_plan.py --data-dir data
.venv/bin/spark-submit --master 'local[2]' pyspark/skew_salting.py
```

`pyspark/course.py` contains the transformations and validation contracts.
Entrypoints are import-safe and always stop the Spark session in `finally`.
No Python UDF or third-party test framework is needed.

### 3. Build and run the four Scala labs

Install sbt only if it is not already available. The launcher uses the version in
the project file. From this track directory:

```bash
(cd scala && sbt -batch package)
JAR=scala/target/scala-2.12/spark-interview-course_2.12-1.0.0.jar

.venv/bin/spark-submit --master 'local[2]' --class course.WordCount "$JAR" data
.venv/bin/spark-submit --master 'local[2]' --class course.SalesPipeline "$JAR" data
.venv/bin/spark-submit --master 'local[2]' --class course.PerformancePlan "$JAR" data
.venv/bin/spark-submit --master 'local[2]' --class course.SkewSalting "$JAR"
```

`spark-sql` is **Provided** in sbt: it is available at compile time, but Spark and
its transitive runtime jars are not bundled in the application jar. `spark-submit`
supplies them. `sbt run` or `java -jar` is **not** the supported launch method.
There is no assembly plugin or fat jar requirement for these examples.

### 4. Run checks

```bash
.venv/bin/python -m unittest discover -s pyspark/tests -v
.venv/bin/spark-submit --master 'local[2]' --class course.SmokeChecks "$JAR" data
```

The Python checks assert exact results, deterministic window ties, bad/missing
amounts, precision/overflow, timestamp parsing, missing keys, duplicate IDs,
unknown customer detection, and salted aggregate equivalence. The Scala smoke
runner covers aggregate/window results and rejection of the bad fixture.
Both skew entrypoints also assert multiset equivalence of the salted **join**.
These are local correctness checks, not distributed failure or benchmark tests.

## Expected results and what each line teaches

### Lab 1: word count

Sources: [`word_count.py`](pyspark/word_count.py),
[`WordCount.scala`](scala/src/main/scala/WordCount.scala).

`read.text` produces one `value` per line. `lower → split → explode → filter`
is row-local tokenization; `groupBy → count` needs cross-partition combination.
`orderBy(count DESC, word ASC)` makes equal counts deterministic.
ASCII letters are intentional: this tokenizer is not a multilingual search engine.

```text
word        count
spark       3
parallel    2
data        1
jobs        1
makes       1
processing  1
scale       1
```

**Predict first:** will `SPARK, spark!` introduce an empty token? `split` can,
but the filter removes it. Would `count().show()` count rows? No: the grouped
`count` is a lazy aggregation; `show` executes it. By contrast, `DataFrame.count()`
without `groupBy` returns a scalar and is an action.

### Lab 2: a trustworthy customer snapshot

Sources: [`sales_pipeline.py`](pyspark/sales_pipeline.py),
[`SalesPipeline.scala`](scala/src/main/scala/SalesPipeline.scala).

```text
customer_id  region  order_id  amount  lifetime_total
c1           US      o2        20.00   30.00
c2           GB      o5        25.00   40.00
c3           US      o6        10.00   15.00
```

Read strings with an explicit CSV layout, validate, parse money as decimal, check
unique IDs and referential integrity, broadcast the small customer dimension,
compute each customer's full-history total, and select the latest order.
`row_number` uses event time then lexical order ID descending as its total-order
tie-breaker. IDs are strings: `o9 > o10` lexically, not numerically. The contract
is deterministic rather than a business assertion that larger IDs are newer.
The total is computed **before** keeping `position = 1`.

The deliberate [`invalid_orders.csv`](data/invalid_orders.csv) fixture must raise
an error, not produce a reduced or rounded report. Validation actions intentionally
add scans; production ingestion should validate an immutable input snapshot and
materialize a clean layer once. This is discussed in the correctness guide.

### Lab 3: physical plans and cache lifecycle

Sources: [`performance_plan.py`](pyspark/performance_plan.py),
[`PerformancePlan.scala`](scala/src/main/scala/PerformancePlan.scala).

```text
Materialized regions: 2
region  revenue
GB      40.00
US      45.00
```

Read the formatted plan before materialization. Find projection/filtering,
`BroadcastHashJoin`/`BroadcastExchange`, partial/final aggregates, and exchanges
for grouping and sorting. After `persist` and `count`, inspect the cached plan
for `InMemoryTableScan`/`InMemoryRelation`. Operator wrappers and adaptive details
vary; exact plan strings are not test assertions.

Caching a two-row report is an educational lifecycle demonstration, **not an
optimization worth applying to this dataset**. CSV projection/filtering still
requires parsing source text; do not equate a filter in the plan with Parquet
row-group skipping or partition pruning.

### Lab 4: hot keys and salting

Sources: [`skew_salting.py`](pyspark/skew_salting.py),
[`SkewSalting.scala`](scala/src/main/scala/SkewSalting.scala).

```text
key     total
cold-0  38
cold-1  41
cold-2  44
cold-3  40
cold-4  36
cold-5  39
cold-6  42
cold-7  38
cold-8  41
cold-9  44
hot     3594
Salted join preserves 1000 rows
```

The 1,000 generated rows have one key on 900 records. First compare plain sum
with `(key, salt)` partial sums followed by an unsalted sum. Then replicate the
hot **dimension row** eight times and assign each hot fact exactly one stable
hash-derived salt. Cold rows use salt zero. Assert equal multisets in both
directions; row counts alone cannot detect value corruption.

The final salt histogram counts **logical buckets**, not executor partitions.
Several buckets can hash to one physical partition; AQE may coalesce partitions.
The total across all keys is **3997**. With tiny data and map-side aggregation,
salting can be slower; the lab proves semantics, not a speedup.

## Troubleshooting without hiding failures

* **Unsupported Java / module errors:** inspect `java -version` and `JAVA_HOME`;
  switch to Java 17 rather than adding arbitrary module flags to Java 26.
* **`NoSuchMethodError` or Scala linkage failures:** inspect `spark-submit --version`;
  remove binary-version mismatch rather than copying Spark jars into the app.
* **Python worker mismatch:** set both PySpark interpreter variables to this venv.
* **Hostname/bind errors locally:** keep `SPARK_LOCAL_IP=127.0.0.1`; do not change
  machine-wide hostname/network configuration.
* **Dependency downloads fail:** compilation/runtime validation is blocked until
  repository access works. Syntax checks alone are not evidence that Spark ran.
* **No UI after exit:** the local UI exists only while the application runs. Enable
  event logging to a course-local directory before launch to retain event logs.
* **CSV schema/data error:** fix or explicitly quarantine the source record with
  a recorded reason. Do not switch to `DROPMALFORMED`, `ignoreCorruptFiles`, or
  `fillna(0)` merely to make a job green.

## References and next steps

Versioned upstream documentation:
[Spark 3.5.6](https://spark.apache.org/docs/3.5.6/),
[SQL tuning](https://spark.apache.org/docs/3.5.6/sql-performance-tuning.html),
[SQL null semantics](https://spark.apache.org/docs/3.5.6/sql-ref-null-semantics.html),
[Structured Streaming](https://spark.apache.org/docs/3.5.6/structured-streaming-programming-guide.html).

Next, test skew on a realistic cluster, storage layout on Parquet, and streaming
restart behavior with an idempotent sink. None of those results are implied by
passing these local labs.
