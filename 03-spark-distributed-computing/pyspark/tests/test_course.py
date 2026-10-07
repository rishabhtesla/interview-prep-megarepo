import sys
import unittest
from decimal import Decimal
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from pyspark.sql import SparkSession, functions as F
from course import (
    CUSTOMER_COLUMNS, DATA_DIR, ORDER_COLUMNS, clean_customers, clean_orders,
    latest_customer_sales, load_sales, read_strings, region_totals, require_equal,
    require_no_rows, salted_totals, skew_facts, word_counts,
)


class CourseTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.spark = (
            SparkSession.builder.master("local[2]").appName("course-tests")
            .config("spark.ui.enabled", "false")
            .config("spark.driver.host", "127.0.0.1")
            .config("spark.sql.session.timeZone", "UTC")
            .config("spark.sql.ansi.enabled", "true")
            .config("spark.sql.legacy.timeParserPolicy", "CORRECTED")
            .config("spark.sql.shuffle.partitions", "2")
            .getOrCreate()
        )
        cls.spark.sparkContext.setLogLevel("ERROR")
        cls.orders, cls.customers = load_sales(cls.spark)

    @classmethod
    def tearDownClass(cls):
        cls.spark.stop()

    def raw_order(self, **overrides):
        values = {
            "order_id": "o1", "customer_id": "c1",
            "amount": "12.00", "event_time": "2025-01-01 00:00:00",
        }
        values.update(overrides)
        return self.spark.createDataFrame(
            [tuple(values[name] for name in ORDER_COLUMNS)],
            ", ".join(f"{name} string" for name in ORDER_COLUMNS),
        )

    def test_word_count_has_stable_tie_order_and_no_empty_word(self):
        actual = word_counts(self.spark.read.text(str(DATA_DIR / "lines.txt"))).collect()
        self.assertEqual(
            [(r.word, r["count"]) for r in actual],
            [("spark", 3), ("parallel", 2), ("data", 1), ("jobs", 1),
             ("makes", 1), ("processing", 1), ("scale", 1)],
        )

    def test_latest_customer_sales(self):
        rows = latest_customer_sales(self.orders, self.customers).collect()
        self.assertEqual(
            [(r.customer_id, r.order_id, r.amount, r.lifetime_total) for r in rows],
            [("c1", "o2", Decimal("20.00"), Decimal("30.00")),
             ("c2", "o5", Decimal("25.00"), Decimal("40.00")),
             ("c3", "o6", Decimal("10.00"), Decimal("15.00"))],
        )

    def test_window_tie_breaker_is_deterministic(self):
        extra = clean_orders(self.raw_order(
            order_id="o9", event_time="2025-01-01 11:00:00", amount="1.00"
        ))
        row = latest_customer_sales(
            self.orders.unionByName(extra), self.customers
        ).filter("customer_id = 'c1'").first()
        self.assertEqual(row.order_id, "o9")
        self.assertEqual(row.lifetime_total, Decimal("31.00"))

    def test_region_totals(self):
        self.assertEqual(
            [(r.region, r.revenue) for r in region_totals(self.orders, self.customers).collect()],
            [("GB", Decimal("40.00")), ("US", Decimal("45.00"))],
        )

    def test_invalid_amounts_fail_instead_of_rounding_or_dropping(self):
        for amount in ("bad", "-1.00", "0", "1.999", "10000000000.00", None):
            with self.subTest(amount=amount), self.assertRaisesRegex(ValueError, "Invalid order"):
                clean_orders(self.raw_order(amount=amount))

    def test_null_and_malformed_keys_fail(self):
        for changes in ({"order_id": None}, {"customer_id": ""},
                        {"order_id": " o1"}, {"customer_id": "unknown"}):
            with self.subTest(changes=changes), self.assertRaisesRegex(ValueError, "Invalid order"):
                clean_orders(self.raw_order(**changes))

    def test_timestamp_contract(self):
        for event_time in (None, "garbage", "2025-02-30 00:00:00", "2025-01-01"):
            with self.subTest(event_time=event_time), self.assertRaisesRegex(ValueError, "Invalid order"):
                clean_orders(self.raw_order(event_time=event_time))
        row = clean_orders(self.raw_order()).select(
            F.date_format("event_ts", "yyyy-MM-dd HH:mm:ss").alias("utc")
        ).first()
        self.assertEqual(row.utc, "2025-01-01 00:00:00")

    def test_invalid_fixture_is_rejected(self):
        raw = read_strings(self.spark, DATA_DIR / "invalid_orders.csv", ORDER_COLUMNS)
        with self.assertRaisesRegex(ValueError, "Invalid order"):
            clean_orders(raw)

    def test_duplicate_orders_fail(self):
        raw = self.raw_order()
        with self.assertRaisesRegex(ValueError, "Duplicate order_id"):
            clean_orders(raw.unionByName(raw))

    def test_customer_quality_and_uniqueness(self):
        raw = read_strings(self.spark, DATA_DIR / "customers.csv", CUSTOMER_COLUMNS)
        with self.assertRaisesRegex(ValueError, "Duplicate customer_id"):
            clean_customers(raw.unionByName(raw))
        with self.assertRaisesRegex(ValueError, "Invalid customer"):
            clean_customers(raw.withColumn("region", F.lit(None).cast("string")))

    def test_unknown_customer_is_not_silently_lost(self):
        with self.assertRaisesRegex(ValueError, "Unknown customer"):
            require_no_rows(
                self.orders.join(self.customers.filter("customer_id != 'c1'"),
                                 "customer_id", "left_anti"),
                "Unknown customer",
            )

    def test_salted_sum_matches_baseline_and_conserves_money(self):
        facts = skew_facts(self.spark)
        expected = facts.groupBy("key").agg(F.sum("amount").alias("total"))
        actual = salted_totals(facts)
        require_equal(expected, actual)
        self.assertEqual(actual.agg(F.sum("total")).first()[0], 3997)
        self.assertEqual(actual.filter("key = 'hot'").first().total, 3594)
        with self.assertRaisesRegex(ValueError, "positive"):
            salted_totals(facts, 0)


if __name__ == "__main__":
    unittest.main(verbosity=2)
