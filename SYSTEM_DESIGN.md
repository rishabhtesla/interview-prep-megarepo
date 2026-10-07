# System design: reasoning from requirements

These are worked design exercises, not claims about features implemented by the
local application. The service and frontend READMEs document the actual runnable
scope. Capacity numbers below are explicitly hypothetical.

## 1. A reusable design method

Start with actors, operations, invariants, and exclusions. Then estimate scale,
define ownership and APIs, draw read/write paths, and analyze failures.

An **invariant** is a property that must remain true, such as "available stock must
never become negative." A **service objective** is a measurable target such as
"99% of successful catalog reads complete within 200 ms." A **mechanism** such
as Redis or Kafka is neither a requirement nor an invariant.

### Capacity estimation

Suppose a system receives 10 million events/day:

```text
Average write rate = 10,000,000 / 86,400 = about 116 events/second
Assumed 10x peak = about 1,160 events/second
At 500 bytes/event = about 5 GB/day of raw payload
At 30 days = about 150 GB raw
At 3 stored replicas = about 450 GB before indexes/metadata/compression
```

These estimates guide a conversation; they are not benchmark results. Clarify
whether peak is measured or assumed, and whether the payload size includes
serialization and storage overhead.

Latency budgets should include queue time, network hops, processing, and storage.
Do not add p99 values from independent components and call the sum an exact
end-to-end p99. Use the sum as a conservative planning aid, then measure traces.

### Consistency vocabulary

| Term | Useful meaning | Frequent mistake |
| --- | --- | --- |
| Atomicity | A transaction's participating changes commit or roll back together | Assuming it includes arbitrary HTTP calls |
| Linearizability | Operations appear to take effect at a single point consistent with real-time order | Calling every primary database read linearizable without examining the system |
| Eventual consistency | Replicas/projections converge under stated conditions once updates stop | Promising a bounded delay without a mechanism or objective |
| Idempotency | Repeating an operation has the same intended effect | Assuming every POST retry is safe |
| At-least-once delivery | A delivered event may be observed more than once | Treating a repeated event as a new business action |
| Exactly-once effect | A defined effect occurs once within specified boundaries | Applying the phrase to an entire system without describing sink/retry semantics |

CAP concerns behavior **during a network partition** under its formal model.
It is not a general instruction to "pick any two" of consistency, availability,
and partition tolerance for every product decision.

## 2. Worked case: checkout without overselling

### Requirements

Customers place orders for physical inventory and pay through an external provider.
The system must not confirm an order without accepted payment and allocated stock.
It should avoid duplicate orders when clients retry.

Assume one primary inventory authority per SKU, moderate traffic initially, and
an external payment API that supports idempotency keys. Exclude global inventory
optimization, refunds after shipment, and fraud scoring from the first iteration.

Clarify whether temporary reservations may expire and whether charging followed
by compensation is acceptable. A business that forbids any transient charge
requires a different payment authorization/capture workflow.

### Ownership and model

| Entity | Important fields | Owner/invariant |
| --- | --- | --- |
| Order | ID, customer, status, amount, currency, version | Order service owns allowed state transitions |
| Order item | Order ID, SKU, quantity, price snapshot | Snapshot price, do not recalculate historical totals from current catalog |
| Inventory | SKU, available quantity, version | Inventory authority prevents negative availability |
| Reservation | Reservation ID, order ID, SKU, quantity, expiry, status | Unique business reservation and explicit lifecycle |
| Payment attempt | Order ID, provider key, status, provider reference | Payment workflow deduplicates and reconciles uncertain outcomes |
| Idempotency record | Caller, key, payload hash, status/result | Database uniqueness handles concurrent retries |
| Outbox event | Event ID, aggregate ID, type, payload, publication status | Committed with the state change that caused the event |

Use integer minor units or appropriately constrained decimal amounts, not binary
floating-point arithmetic for authoritative money calculations. Currency and rounding
rules are part of the contract.

### API shape

```http
POST /orders
Idempotency-Key: caller-generated-opaque-key
Content-Type: application/json

{"items":[{"sku":"book-123","quantity":2}],"currency":"USD"}
```

Authenticate the caller; do not trust a body-supplied customer ID as authorization.
Store a canonical request fingerprint with the key. Same key/same request returns
the same operation result or status; same key/different request is a conflict.
Define expiration and retention of keys explicitly.

Return an operation/order identifier when processing is asynchronous. A `202`
response is acceptance, not payment confirmation.

### Reserve stock atomically

A simplified single-SKU reservation uses a conditional update:

```sql
UPDATE inventory
SET available = available - :quantity
WHERE sku = :sku AND available >= :quantity;
```

Validate positive quantity, check affected-row count, and write the reservation
record in the same database transaction. A unique reservation key is necessary
to prevent retrying the update from decrementing stock twice.

For multiple SKUs in one database, use one transaction and consistent lock ordering.
If any allocation fails, roll back all allocations. Across independent inventory
authorities, local transactions alone cannot provide one global atomic reservation.

### Workflow

```text
Client -> Order API -> local transaction: pending order + outbox event
Outbox publisher -> reservation workflow
Inventory -> reserve using unique reservation identity
Payment provider -> authorize/capture using stable payment identity
Order -> confirm only after required outcomes are recorded
Failure/expiry -> release reservation and, if necessary, refund/void payment
Reconciliation -> repair unresolved/uncertain transitions
```

An outbox prevents the database-commit/message-send gap by recording intent in the
same local transaction. The publisher can still deliver twice after a crash.
Consumers need deduplication or idempotent state transitions.

A saga is a sequence of local transactions and compensations, not a distributed
ACID transaction. Compensation can also fail and require retries or human review.

### Failure analysis

| Failure | Why it is tricky | Response |
| --- | --- | --- |
| Client times out after commit | Server outcome is unknown to client | Retry with same idempotency key or query operation status |
| Payment request times out | Payment may have succeeded remotely | Query provider using stable identity; do not blindly charge again |
| Publisher crashes after sending | Event may be resent | Deduplicate by event ID and protect domain transitions |
| Payment succeeds after reservation expiry | Late success conflicts with released stock | State/version guard, reconcile, and refund or reacquire according to policy |
| Compensation fails | Intermediate state persists | Durable retry queue, alerting, explicit manual resolution |
| Catalog price changes mid-checkout | Recalculation may charge unexpectedly | Expiring quote or server-approved price snapshot |

Monitor order age by state, reservation expiry, payment uncertainty, outbox lag,
duplicate rates, and compensation backlog. A green CPU dashboard does not prove
orders are completing correctly.

### Defend the architecture

For a small team and moderate traffic, a modular monolith with one relational
database may provide simpler transactions and operations. Separate services become
valuable when domain ownership, independent deployment, or scaling justifies their
failure and coordination costs.

**Follow-up:** "Can we just add a distributed lock?"

Locks have leases, ownership, expiry, and failure semantics. They do not make a
remote payment participate in a database transaction. Preserve invariants in the
authoritative store and reason about stale lock holders/fencing where relevant.

## 3. Worked case: daily analytics with late data

### Requirements and assumptions

Process immutable purchase events into daily revenue by region. Support late arrivals,
duplicate delivery, and corrected source events. Reports should be available within
one hour of a scheduled daily run; this is batch freshness, not real-time latency.

Suppose there are 10 million events/day at 500 bytes each. Thirty days of raw input
is about 150 GB before replication and metadata. Ask about key skew, distinct region
count, retention, and correction frequency before choosing partition strategies.

### Event contract

```text
event_id
purchase_id
event_version
event_time_utc
ingested_at_utc
region
amount_minor_units
currency
event_type
```

An event ID deduplicates delivery. A purchase ID plus version identifies revisions
of business state. Two different purchases with the same amount and timestamp
are not duplicates. State how cancellations and refunds affect revenue.

### Pipeline

```text
Source -> append-only raw storage
       -> schema/data-quality validation
       -> delivery deduplication + version resolution
       -> canonical purchase facts
       -> daily region/currency aggregation
       -> versioned report publication
```

Store invalid rows separately with reasons and metrics, or fail the job according
to an explicit policy. Silently dropping invalid money values changes the report.
Retain enough lineage to explain a total.

### Computation and partitioning

Partition raw data by ingestion date for efficient arrival-based discovery.
Maintain event-date metadata or a canonical table layout for recomputing affected
business days. Ingestion date and event date answer different questions.

Avoid a directory per customer for high-cardinality identifiers. It creates tiny
partitions and file-management overhead. Choose partition columns based on read
patterns and manageable cardinality; tune file size using measured output.

A join to a region/customer dimension must specify whether to use current attributes
or historical attributes valid at event time. Otherwise, a customer's move can
silently rewrite historical regional revenue.

### Reruns and publication

Do not append the same day's aggregate on every retry. Write a new version to a
staging location, validate it, then publish it through an atomic metadata change
or a transactional table commit supported by the storage system.

An object-store rename is not universally a single atomic transaction. Name the
actual publication mechanism rather than assuming filesystem semantics everywhere.

For late data, identify affected dates and recompute/replace them, or apply durable,
deduplicated corrections. Keep the previous published version until the new version
is complete. Consumers should see a coherent report version.

### Streaming follow-up

For lower latency, discuss event-time processing, checkpoints, state, watermarks,
and sink guarantees. A watermark helps bound state and controls late-data behavior
for supported operations; it is not a promise that all late events are accepted
forever or that results are complete in the business sense.

Checkpointed offsets alone do not deduplicate arbitrary external HTTP effects.
Explain the source, engine, state, and sink boundary of any exactly-once claim.

### Operational questions

| Symptom | Investigation |
| --- | --- |
| Revenue doubles | Duplicate delivery, many-to-many join, repeated publication, or legitimate source correction? |
| One stage is slow | Partition sizes, shuffle, skew, spill, expensive UDF, or storage throughput? |
| Driver crashes | Collected too much data, large query plan, broadcast preparation, or metadata pressure? |
| Yesterday's report changes | Late event, timezone boundary, dimension history, or nondeterministic version selection? |
| Output has thousands of tiny files | Partition cardinality, shuffle partition count, write frequency, and compaction policy |

**Exercise:** A single tenant produces 40% of events. Explain why tenant-based
partitioning may be poor for processing even if tenant filtering is important for reads.

**Worked answer:** One tenant partition can dominate work and memory. Separate
logical isolation/filtering from physical processing distribution. Use a partition
strategy that permits the hot tenant to span processing partitions, then cluster
or organize output for reads if the table format supports it. Salting an associative
aggregation may help; blindly salting arbitrary window orderings will not preserve
the same computation.

## 4. Worked case: URL shortener

### Requirements

Create short links and resolve them quickly. Assume public links, optional expiry,
100 million new links/year, and a 100:1 read/write ratio. Exclude arbitrary custom
aliases and analytics from the first version.

Average writes are roughly 3.2/sec and average reads roughly 320/sec. Peaks and hot
links matter more than these averages. A small initial system may not need sharding.

### Model and API

```text
link(code UNIQUE, destination_url, created_at, expires_at, disabled_at)
POST /links -> generated code
GET /{code} -> redirect or not-found/expired response
```

Validate allowed URL schemes, length, and destination policy. A public shortener
needs abuse reporting, throttling, and malicious-link handling. A redirect endpoint
does not itself fetch the destination; a preview crawler introduces separate SSRF
risks and needs network restrictions.

### Identifier choices

| Choice | Advantage | Cost |
| --- | --- | --- |
| Encoded database sequence | Compact, collision-free within allocation scheme | Predictable/enumerable, requires allocation strategy at scale |
| Random base62 code | Decentralized generation and less predictability | Collision handling and sufficient entropy required |
| Hash of URL | Deterministic mapping possibility | Collision resolution, normalization, and privacy/product semantics |

Base62 with seven characters has `62^7`, about 3.52 trillion possible values.
This does **not** mean collisions are negligible for 100 million random codes:
the birthday approximation gives expected colliding pairs
`n(n-1)/(2M)`, about 1,420 at that scale. Use a unique constraint plus retry,
and choose length based on traffic, enumeration risk, and retry cost.

### Read path and caching

```text
Resolve -> cache lookup -> database lookup on miss -> cache with bounded TTL
        -> enforce disabled/expiry semantics -> redirect
```

Choose redirect status deliberately. Permanent redirects may be cached by browsers
and intermediaries, making changes/revocation harder. Use temporary redirects when
destination mutability and analytics policy require fresh resolution.

TTL is not immediate invalidation. If a disabled link must stop immediately,
coordinate invalidation or authoritative checks and define the acceptable propagation
window. Cache expiry must not allow a link to outlive its own expiration.

### Scaling and failures

Measure hit rate, hot-key load, redirect latency, database miss load, invalid-code
traffic, and creation collision retries. Add rate limiting against random-code
probing and protect the database against a cache outage. Negative caching can help
but must be bounded and not hide newly created links for too long.

Do not shard merely because the interview includes "millions." First estimate
storage, working set, throughput, and one-node capacity.

## 5. Capstone: multi-user interview study platform

This design extends the local teaching application conceptually. It is a practice
exercise, not a claim that the extensions already exist.

### Product contract

Users browse lessons, record completion, revisit difficult topics, and view weekly
progress. Their progress should survive devices and restarts. Only the authenticated
user can modify their own progress. A daily analytics job reports aggregate activity
without exposing private user notes.

### Proposed model

```text
User(id, identity_provider_subject)
Lesson(id, track_id, title, content_version)
Progress(user_id, lesson_id, completed, updated_at, version)
ActivityEvent(event_id, user_id, lesson_id, event_type, occurred_at)
```

Use a unique constraint on `(user_id, lesson_id)`. Completion is current state;
activity is history. Repeated "set completed=true" requests should not necessarily
create repeated "new completion" business events.

### Proposed API

```http
GET /lessons?track=java&after=opaque-cursor
GET /me/progress
PUT /me/progress/{lessonId}
If-Match: "current-version"

{"completed":true}
```

Derive the user from authenticated server context, not a trusted request parameter.
Use version preconditions if the product wants conflict detection. Return the
documented conflict/precondition response and latest representation on stale edits.
If the product instead chooses last-write-wins, explain the lost-update behavior
and its acceptability; do not pretend it detects conflicts.

### UI behavior

Render loading, empty, failure, and success distinctly. Disable or track an in-flight
update per item. If showing optimistic changes, retain enough previous state to
reconcile failure, and ensure an older failed request does not roll back a newer
successful one.

Announce status changes accessibly and preserve focus. A disabled button without
an explanation can hide why an action is unavailable.

### Event and analytics flow

Commit progress and an outbox event together if the event is required to match
the database transition. Deduplicate downstream by event identity. Aggregate using
an explicit timezone and privacy policy. A local date in the browser is not an
unambiguous global reporting day.

### Deliverable and review

Before implementing an extension, write one page containing the API contract,
invariant, state transition, failure table, and test cases. After implementation,
draw the actual path and label proposals separately.

Review against these questions:

1. Can another user read or modify this user's progress?
2. Can concurrent requests create duplicates or erase a newer update?
3. What does the UI show after an ambiguous timeout?
4. Is the analytics event committed consistently with the progress transition?
5. Can a failed daily job be rerun without double-counting?
6. What metrics distinguish a healthy process from a working user journey?

## 6. Design anti-patterns to avoid

- Choosing a message broker before deciding whether asynchronous behavior is acceptable.
- Calling every service independently scalable while keeping one shared write bottleneck.
- Treating caches as authoritative without an invalidation/consistency design.
- Retrying at every layer with no deadline, multiplying load during an outage.
- Claiming a saga automatically rolls back all external effects.
- Sharding before identifying a real capacity or ownership boundary.
- Calling a local demo production-ready because it uses a popular framework.

Good design explains which complexity is necessary now, which is postponed, and
what evidence would justify the next step.
