-- Validate the join cardinality before aggregating.
SELECT c.id, c.name, SUM(o.amount_cents) AS total_cents
FROM customers c
JOIN orders o ON o.customer_id = c.id
GROUP BY c.id, c.name;

-- Inspect the target database's plan before claiming the index helps.
EXPLAIN SELECT *
FROM orders
WHERE customer_id = 'c1'
ORDER BY created_at DESC;
