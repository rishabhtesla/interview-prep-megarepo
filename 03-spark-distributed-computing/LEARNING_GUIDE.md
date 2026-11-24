# Spark learning guide: from one transformation to a reliable pipeline

This track has equivalent PySpark and Scala examples. Run Python first, then
run the Scala version and separate Spark behavior from language syntax.

## Study setup

Follow the pinned setup in [README.md](README.md), then run:

```bash
.venv/bin/python -m unittest discover -s pyspark/tests -v
```

The local mode is for learning semantics and correctness. It is not proof of
cluster performance.

## Lesson order

| Step | Source/docs | What happens | Change exercise | Interview checkpoint |
|---:|---|---|---|---|
| 1 | [`ZeroToHero`](MASTERCLASS.md), `pyspark/zero_to_hero.py` | Create a schema, transform rows, join, window, cache, inspect, and execute a plan | Change a filter or tie-breaker and predict the report | Distinguish a lazy transformation from the action that runs it |
| 2 | `word_count.py`, execution guide | Read lines, tokenize, explode, group, count, and sort | Add punctuation or an empty line | Identify lazy transformations and the action that executes them |
| 3 | `sales_pipeline.py`, correctness guide | Parse a schema, validate money and IDs, join, aggregate, and choose a deterministic latest row | Add a duplicate ID or unknown customer | Explain join cardinality and why validation must fail loudly |
| 4 | `performance_plan.py`, performance guide | Inspect a logical/physical plan and materialize a reused result | Remove caching or move an action | Identify an exchange/shuffle and explain its cost |
| 5 | `skew_salting.py`, performance guide | Split a hot key into salts, aggregate/join, and remove the salt | Make one key dominate the input | Explain why more executors alone may not solve skew |
| 6 | streaming section | Model watermarks, state, late data, and output semantics | Delay an event beyond the watermark | Explain state growth and late-data policy |
| 7 | exercises and interview questions | Modify a transformation while preserving the contract | Write a new invariant before the code | Explain retry behavior and external side effects |

## Read every Spark job in this order

```text
input schema
-> validation
-> lazy transformations
-> shuffle boundary
-> action
-> output contract
-> cleanup / checkpoint / retry behavior
```

Ask “where does the data move?” before asking “which API did the author use?”
`groupBy`, `join`, `distinct`, `orderBy`, and many window operations can move
data across partitions.

## Spark revision card

Be able to explain:

- Driver, executor, task, partition, stage, and job.
- Transformation versus action.
- Narrow versus wide dependency.
- Broadcast join versus shuffle join.
- Partition count versus number of executors.
- Data skew versus insufficient parallelism.
- Cache storage level and materialization.
- Task retry versus idempotent output.
- Event time, processing time, watermark, and state.
- Why a local result is not a production benchmark.
