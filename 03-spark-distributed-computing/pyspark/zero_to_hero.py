"""Beginner-first Spark tour: schema, transformations, actions, joins, windows, and plans."""

from pyspark.sql import Window, functions as F, types as T

from course import run_batch


ORDER_SCHEMA = T.StructType(
    [
        T.StructField("order_id", T.StringType(), False),
        T.StructField("customer_id", T.StringType(), False),
        T.StructField("topic", T.StringType(), False),
        T.StructField("amount_cents", T.IntegerType(), False),
        T.StructField("event_time", T.StringType(), False),
    ]
)


def build_orders(spark):
    """Create a small deterministic DataFrame with an explicit schema."""
    rows = [
        ("o1", "c1", "java", 1_200, "2025-01-01 09:00:00"),
        ("o2", "c1", "spring", 2_000, "2025-01-02 09:00:00"),
        ("o3", "c2", "spark", 1_500, "2025-01-01 10:00:00"),
        ("o4", "c2", "java", 900, "2025-01-03 10:00:00"),
    ]
    return spark.createDataFrame(rows, ORDER_SCHEMA)


def build_customers(spark):
    schema = T.StructType(
        [
            T.StructField("customer_id", T.StringType(), False),
            T.StructField("name", T.StringType(), False),
            T.StructField("region", T.StringType(), False),
        ]
    )
    return spark.createDataFrame(
        [("c1", "Ada", "US"), ("c2", "Grace", "GB")], schema
    )


def build_report(spark):
    """Return the latest order and lifetime total for every customer."""
    orders = build_orders(spark)
    customers = build_customers(spark)

    # Transformations are lazy: these expressions build a plan but do not run it.
    typed_orders = orders.withColumn(
        "event_ts", F.to_timestamp("event_time", "yyyy-MM-dd HH:mm:ss")
    ).withColumn(
        "amount", (F.col("amount_cents") / F.lit(100)).cast("decimal(10,2)")
    )
    java_orders = typed_orders.filter(F.col("topic") == "java")
    selected_columns = java_orders.select("order_id", "customer_id", "amount")

    # A broadcast join is appropriate when the dimension is small enough to fit
    # in each executor. It avoids shuffling the large side in this example.
    joined = typed_orders.join(F.broadcast(customers), "customer_id", "inner")

    # The window keeps every row while adding a per-customer calculation.
    by_customer = Window.partitionBy("customer_id")
    newest_first = by_customer.orderBy(F.col("event_ts").desc(), F.col("order_id").desc())
    report = (
        joined.withColumn("lifetime_cents", F.sum("amount_cents").over(by_customer))
        .withColumn("position", F.row_number().over(newest_first))
        .filter(F.col("position") == 1)
        .select(
            "customer_id",
            "name",
            "region",
            "order_id",
            "amount",
            F.col("lifetime_cents").alias("lifetime_cents"),
        )
        .orderBy("customer_id")
    )

    # This action executes the lazy plan. Cache only when the same result is
    # reused; always unpersist when the lesson is finished with it.
    report.cache()
    report.count()
    return report, selected_columns


def lesson(spark, _data_dir):
    print("\n1) Inspect the schema")
    orders = build_orders(spark)
    orders.printSchema()

    print("\n2) Build and execute a DataFrame plan")
    report, java_orders = build_report(spark)
    report.show(truncate=False)
    java_orders.show(truncate=False)

    # `explain` exposes how Spark plans the transformations. Look for a
    # BroadcastExchange, a window operation, and exchanges caused by grouping.
    print("\n3) Explain the physical plan")
    report.explain(mode="formatted")

    # The assertions are the output contract for this local lesson.
    actual = [
        (row.customer_id, row.order_id, row.lifetime_cents)
        for row in report.collect()
    ]
    expected = [("c1", "o2", 3_200), ("c2", "o4", 2_400)]
    if actual != expected:
        raise AssertionError(f"latest-order contract failed: {actual}")
    if java_orders.count() != 2:
        raise AssertionError("filter contract failed")

    print(f"partitions before explicit repartition: {orders.rdd.getNumPartitions()}")
    print(
        "partitions after explicit repartition:",
        orders.repartition("customer_id").rdd.getNumPartitions(),
    )
    report.unpersist()
    print("Zero-to-hero PySpark tour passed.")


if __name__ == "__main__":
    run_batch("spark-zero-to-hero", lesson)
