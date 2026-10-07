"""Example 3: inspect broadcast, exchange, aggregate, and cache plans."""
from pyspark import StorageLevel
from course import load_sales, region_totals, run_batch


def job(spark, data_dir):
    orders, customers = load_sales(spark, data_dir)
    report = region_totals(orders, customers)
    report.explain(mode="formatted")
    cached = report.persist(StorageLevel.MEMORY_AND_DISK)
    try:
        print(f"Materialized regions: {cached.count()}")
        cached.show(truncate=False)
        cached.explain(mode="formatted")
    finally:
        cached.unpersist()


if __name__ == "__main__":
    run_batch("course-performance-plan", job)
