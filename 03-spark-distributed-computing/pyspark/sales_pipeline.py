"""Example 2: fail-fast quality checks, dimension join and deterministic window."""
from course import latest_customer_sales, load_sales, run_batch


def job(spark, data_dir):
    orders, customers = load_sales(spark, data_dir)
    latest_customer_sales(orders, customers).show(truncate=False)


if __name__ == "__main__":
    run_batch("course-sales-pipeline", job)
