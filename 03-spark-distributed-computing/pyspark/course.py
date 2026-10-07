"""Shared, SQL-expression-only transformations: no Python worker UDFs."""
from pathlib import Path

from pyspark.sql import SparkSession, Window, functions as F, types as T


DATA_DIR = Path(__file__).resolve().parents[1] / "data"
ORDER_COLUMNS = ["order_id", "customer_id", "amount", "event_time"]
CUSTOMER_COLUMNS = ["customer_id", "region"]


def session(name):
    return (
        SparkSession.builder.appName(name)
        .config("spark.sql.session.timeZone", "UTC")
        .config("spark.sql.ansi.enabled", "true")
        .config("spark.sql.legacy.timeParserPolicy", "CORRECTED")
        .config("spark.sql.shuffle.partitions", "4")
        .config("spark.sql.adaptive.enabled", "true")
        .getOrCreate()
    )


def read_strings(spark, path, columns):
    schema = T.StructType([T.StructField(name, T.StringType(), True) for name in columns])
    return (
        spark.read.schema(schema)
        .option("header", "true")
        .option("enforceSchema", "false")
        .option("mode", "FAILFAST")
        .csv(str(path))
    )


def require_no_rows(frame, message):
    # Do not log entire bad records, which may contain sensitive source data.
    if frame.limit(1).count():
        raise ValueError(message)


def require_unique(frame, key):
    require_no_rows(frame.groupBy(key).count().filter("count > 1"), f"Duplicate {key}")


def clean_orders(raw):
    parsed = (
        raw.withColumn("amount_value", F.expr("try_cast(amount as decimal(12,2))"))
        .withColumn(
            "event_ts",
            F.try_to_timestamp("event_time", F.lit("yyyy-MM-dd HH:mm:ss")),
        )
    )
    valid = (
        F.col("order_id").rlike(r"^o[0-9]+$")
        & F.col("customer_id").rlike(r"^c[0-9]+$")
        & F.col("amount").rlike(r"^[0-9]+(\.[0-9]{1,2})?$")
        & (F.col("amount_value") > 0)
        & F.col("event_ts").isNotNull()
        & (F.date_format("event_ts", "yyyy-MM-dd HH:mm:ss") == F.col("event_time"))
    )
    require_no_rows(
        parsed.filter(~F.coalesce(valid, F.lit(False))),
        "Invalid order: required IDs, positive decimal(12,2) amount with <=2 "
        "fractional digits, and UTC yyyy-MM-dd HH:mm:ss timestamp are required",
    )
    result = parsed.select(
        "order_id", "customer_id", F.col("amount_value").alias("amount"), "event_ts"
    )
    require_unique(result, "order_id")
    return result


def clean_customers(raw):
    valid = F.col("customer_id").rlike(r"^c[0-9]+$") & F.col("region").rlike(r"^[A-Z]{2}$")
    require_no_rows(
        raw.filter(~F.coalesce(valid, F.lit(False))),
        "Invalid customer: customer_id and two-letter uppercase region required",
    )
    require_unique(raw, "customer_id")
    return raw


def load_sales(spark, data_dir=DATA_DIR):
    root = Path(data_dir)
    orders = clean_orders(read_strings(spark, root / "orders.csv", ORDER_COLUMNS))
    customers = clean_customers(
        read_strings(spark, root / "customers.csv", CUSTOMER_COLUMNS)
    )
    require_no_rows(
        orders.join(customers, "customer_id", "left_anti"),
        "Unknown customer_id: inner join would lose orders",
    )
    return orders, customers


def word_counts(lines):
    words = lines.select(
        F.explode(F.split(F.lower(F.col("value")), r"[^a-z]+")).alias("word")
    )
    return (
        words.filter(F.col("word") != "")
        .groupBy("word").count()
        .orderBy(F.desc("count"), "word")
    )


def latest_customer_sales(orders, customers):
    joined = orders.join(F.broadcast(customers), "customer_id", "inner")
    by_customer = Window.partitionBy("customer_id")
    latest = by_customer.orderBy(F.desc("event_ts"), F.desc("order_id"))
    return (
        joined.withColumn("lifetime_total", F.sum("amount").over(by_customer))
        .withColumn("position", F.row_number().over(latest))
        .filter("position = 1")
        .select("customer_id", "region", "order_id", "amount", "lifetime_total")
        .orderBy("customer_id")
    )


def region_totals(orders, customers):
    return (
        orders.select("customer_id", "amount", "event_ts")
        .filter(F.col("event_ts") >= F.lit("2025-01-01").cast("timestamp"))
        .join(F.broadcast(customers.select("customer_id", "region")), "customer_id")
        .groupBy("region").agg(F.sum("amount").alias("revenue"))
        .orderBy("region")
    )


def skew_facts(spark):
    return spark.range(1000).select(
        F.col("id").alias("event_id"),
        F.when(F.col("id") < 900, F.lit("hot"))
        .otherwise(F.concat(F.lit("cold-"), (F.col("id") % 10).cast("string"))).alias("key"),
        ((F.col("id") % 7) + 1).alias("amount"),
    )


def salted_totals(facts, buckets=8):
    if buckets < 1:
        raise ValueError("buckets must be positive")
    salted = facts.withColumn(
        "salt",
        F.when(F.col("key") == "hot", F.pmod(F.xxhash64("event_id"), F.lit(buckets)))
        .otherwise(F.lit(0)),
    )
    partial = salted.groupBy("key", "salt").agg(F.sum("amount").alias("subtotal"))
    return partial.groupBy("key").agg(F.sum("subtotal").alias("total"))


def require_equal(left, right):
    require_no_rows(left.exceptAll(right), "Unexpected extra output rows")
    require_no_rows(right.exceptAll(left), "Missing output rows")


def run_batch(name, job):
    import argparse

    parser = argparse.ArgumentParser(description=name)
    parser.add_argument("--data-dir", default=str(DATA_DIR))
    args = parser.parse_args()
    spark = session(name)
    spark.sparkContext.setLogLevel("WARN")
    try:
        job(spark, Path(args.data_dir))
    finally:
        spark.stop()
