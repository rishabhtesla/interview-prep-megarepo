"""Lesson 1: small, deterministic Python functions."""

from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal


@dataclass(frozen=True)
class Order:
    customer_id: str
    amount: Decimal
    completed: bool

    def __post_init__(self) -> None:
        if not self.customer_id:
            raise ValueError("customer_id is required")
        if self.amount < 0:
            raise ValueError("amount cannot be negative")


def completed_totals(orders: list[Order]) -> dict[str, Decimal]:
    totals: dict[str, Decimal] = {}
    for order in orders:
        if order.completed:
            totals[order.customer_id] = (
                totals.get(order.customer_id, Decimal("0")) + order.amount
            )
    return totals


def append_tag(tags: list[str] | None = None, tag: str = "interview") -> list[str]:
    """Create a fresh list; never share a mutable default between calls."""

    result = list(tags) if tags is not None else []
    result.append(tag)
    return result


def main() -> None:
    orders = [
        Order("c1", Decimal("10.50"), completed=True),
        Order("c1", Decimal("4.50"), completed=False),
        Order("c2", Decimal("8.00"), completed=True),
    ]
    print(completed_totals(orders))
    print(append_tag())
    print(append_tag())


if __name__ == "__main__":
    main()
