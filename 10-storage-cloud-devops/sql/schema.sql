CREATE TABLE customers (
    id VARCHAR(40) PRIMARY KEY,
    name VARCHAR(120) NOT NULL
);

CREATE TABLE orders (
    id VARCHAR(40) PRIMARY KEY,
    customer_id VARCHAR(40) NOT NULL REFERENCES customers(id),
    amount_cents BIGINT NOT NULL CHECK (amount_cents > 0),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX orders_customer_created_idx
    ON orders (customer_id, created_at);
