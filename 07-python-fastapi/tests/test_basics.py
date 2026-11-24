from decimal import Decimal
import unittest

from examples.basics_import import completed_totals, Order


class BasicsTest(unittest.TestCase):
    def test_completed_totals_ignores_incomplete_orders(self) -> None:
        orders = [
            Order("c1", Decimal("10.00"), True),
            Order("c1", Decimal("2.00"), False),
            Order("c2", Decimal("5.50"), True),
        ]
        self.assertEqual(
            {"c1": Decimal("10.00"), "c2": Decimal("5.50")},
            completed_totals(orders),
        )

    def test_negative_amount_is_rejected(self) -> None:
        with self.assertRaises(ValueError):
            Order("c1", Decimal("-0.01"), True)


if __name__ == "__main__":
    unittest.main()
