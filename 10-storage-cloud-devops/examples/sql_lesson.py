"""Run a small relational lesson without installing PostgreSQL or MySQL."""

import sqlite3


def main() -> None:
    connection = sqlite3.connect(":memory:")
    connection.execute("PRAGMA foreign_keys = ON")
    connection.executescript(
        """
        CREATE TABLE customers (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL
        );
        CREATE TABLE orders (
            id TEXT PRIMARY KEY,
            customer_id TEXT NOT NULL REFERENCES customers(id),
            amount_cents INTEGER NOT NULL CHECK (amount_cents > 0)
        );
        INSERT INTO customers VALUES ('c1', 'Asha'), ('c2', 'Ravi');
        INSERT INTO orders VALUES
            ('o1', 'c1', 1000), ('o2', 'c1', 2500), ('o3', 'c2', 700);
        CREATE INDEX orders_customer_idx ON orders(customer_id);
        """
    )
    rows = connection.execute(
        """
        SELECT c.id, c.name, SUM(o.amount_cents) AS total_cents
        FROM customers c
        JOIN orders o ON o.customer_id = c.id
        GROUP BY c.id, c.name
        ORDER BY c.id
        """
    ).fetchall()
    plan = connection.execute(
        "EXPLAIN QUERY PLAN SELECT * FROM orders WHERE customer_id = 'c1'"
    ).fetchall()
    print(rows)
    print("query plan:", plan)
    try:
        connection.execute("INSERT INTO orders VALUES ('bad', 'missing', 1)")
    except sqlite3.IntegrityError as error:
        print("constraint rejected invalid foreign key:", error)
    connection.close()


if __name__ == "__main__":
    main()
