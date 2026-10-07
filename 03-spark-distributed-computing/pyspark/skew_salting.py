"""Example 4: two-phase salted sum and correctness-preserving salted join."""
from pyspark.sql import functions as F
from course import require_equal, run_batch, salted_totals, skew_facts


def job(spark, _data_dir):
    facts = skew_facts(spark)
    plain = facts.groupBy("key").agg(F.sum("amount").alias("total"))
    salted = salted_totals(facts)
    require_equal(plain, salted)
    salted.orderBy("key").show(20, truncate=False)

    dimension = facts.select("key").distinct().withColumn("label", F.upper("key"))
    expanded = dimension.withColumn(
        "salt",
        F.explode(
            F.when(F.col("key") == "hot", F.sequence(F.lit(0), F.lit(7)))
            .otherwise(F.array(F.lit(0)))
        ),
    )
    salted_facts = facts.withColumn(
        "salt",
        F.when(F.col("key") == "hot", F.pmod(F.xxhash64("event_id"), F.lit(8)))
        .otherwise(F.lit(0)),
    )
    joined = salted_facts.join(expanded, ["key", "salt"]).select(
        "event_id", "key", "amount", "label"
    )
    expected = facts.join(dimension, "key").select("event_id", "key", "amount", "label")
    require_equal(expected, joined)
    print(f"Salted join preserves {joined.count()} rows")
    salted_facts.filter("key = 'hot'").groupBy("salt").count().orderBy("salt").show()


if __name__ == "__main__":
    run_batch("course-skew-salting", job)
