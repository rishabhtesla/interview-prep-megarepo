# System design and behavioral interview answers

## A repeatable design flow

```text
clarify requirements
  -> define APIs and invariants
  -> estimate capacity
  -> choose ownership/storage
  -> draw read/write paths
  -> analyze consistency/failure/security
  -> define observability and rollout
```

Start with users, operations, correctness, latency, throughput, retention,
availability, and exclusions. Do not start by naming Kafka, Redis, or
Kubernetes.

## Capacity example

For 10 million events/day:

```text
average = 10,000,000 / 86,400 ≈ 116 events/s
10x peak ≈ 1,160 events/s
500 bytes/event ≈ 5 GB/day raw
30 days ≈ 150 GB raw before indexes/replicas/compression
```

State which numbers are measured versus assumed. Then ask whether peak is
bursty, whether payload includes metadata, how many replicas/partitions are
needed, and what retention/deletion policy applies.

## Common system-design questions and answers

### Design a partner-feed ingestion engine.

**Answer outline:**

```text
partner adapters -> schema validation -> canonical event
  -> deduplication/idempotency -> durable queue/log
  -> normalized storage/projection -> downstream consumers
```

Use an anti-corruption adapter per partner, versioned canonical schema,
correlation/event IDs, bounded retries and dead letters, and an outbox/CDC
strategy if a database write and event publication must agree. Partition by
stable business key when per-entity ordering matters. Store raw/quarantined
payloads for replay without trusting them as normalized data.

### Design a study-progress service.

**Answer outline:** Use a progress database as the source of truth, idempotent
PUT semantics by topic/learner key, optimistic versioning for concurrent writes,
and a read API that distinguishes missing progress from dependency failure.
If catalog validation is remote, set a timeout, map 404 versus 503, and never
pretend a failed validation write succeeded.

### How do you guarantee idempotency?

**Answer:** Define an idempotency key and operation scope, persist the key with
the result or business mutation under a unique constraint, and return the
original result for a repeat. Expire keys only when the business retry window
allows it. A random request ID without durable storage is not idempotency.

### How do you handle partial failure?

**Answer:** Identify which steps committed, make retries safe, expose an
ambiguous outcome honestly, and provide reconciliation/replay. Use timeouts,
bounded retries, circuit breakers, DLQs, compensating actions, and operational
metrics. Do not return an empty success response because a dependency failed.

### Strong versus eventual consistency?

**Answer:** Strong consistency gives a read/write contract where the latest
accepted write is visible according to a defined boundary. Eventual consistency
allows replicas/projections to converge later and often improves availability,
latency, or decoupling. State the user-visible invariant and how conflicts,
staleness, and repair are handled.

## Reliability and security checklist

| Question | Evidence to name |
|---|---|
| What can fail? | dependency, network, process, disk, schema, operator |
| How is it detected? | logs, metrics, traces, health, lag, alerts |
| How is it contained? | timeout, bulkhead, rate limit, breaker, queue |
| How is it recovered? | retry, replay, compensation, rollback, restore |
| Who is authorized? | identity, tenant/resource check, least privilege |
| How is data protected? | TLS, encryption, retention, redaction, audit |
| How is it shipped? | backward compatibility, probes, canary, rollback |

## Behavioral answers from the resume

Use truthful STAR stories. Do not invent production metrics; explain the
measurement and your personal contribution.

### Runtime reduction story

```text
Situation: Spark geometry work took about 7 hours.
Task: Find the executor/partition bottleneck without changing output semantics.
Action: Separate nested geometry work into parallelizable partition stages,
        inspect plans/metrics, and benchmark representative data.
Result: Runtime reduced to about 2.5 hours (~60%), with correctness checks.
Follow-up: Explain skew, shuffle, cost measurement, and rollback.
```

### Duplicate-event reduction story

```text
Situation: Incident feeds produced duplicate events.
Task: Merge/deduplicate by region and incident identity.
Action: Define event identity/merge rules, own per-key state, persist recovery
        state, and test duplicate/replay/failover cases.
Result: About 40% fewer duplicate incident events.
Follow-up: Explain late events, cluster rebalance, and idempotent publication.
```

### Full-stack mutation visualizer story

```text
Situation: Operators needed to inspect three-stage GeoJSON mutations.
Task: Make differences visible without hiding backend/API failures.
Action: React state and accessible UI -> REST API -> mutation stages; add
        loading/error/retry tests and map-layer contracts.
Result: Faster operational inspection with explicit failure behavior.
Follow-up: Explain state ownership, GeoJSON validation, rendering performance,
          and authorization.
```

## Practice configuration

For every design, write a small contract before implementation:

```yaml
service:
  timeout: 2s
  retry:
    maxAttempts: 3
    backoff: exponential-with-jitter
  idempotency:
    key: request-id
    storage: durable-unique-record
observability:
  correlationId: required
  metrics: latency,error_rate,queue_lag
security:
  transport: tls
  authorization: resource-owner-check
```

This is a reasoning template, not a drop-in deployment file. Replace each
placeholder with a measured SLO, ownership decision, and platform-specific
configuration.

## Final answer format

```text
I would clarify ...
The invariant is ...
The request/data flow is ...
The storage/partition key is ...
The normal path is ...
The failure path is ...
The consistency/idempotency boundary is ...
I would measure ...
I would roll it out by ...
```

## Revision card

```text
Clarify before designing.
Quantify average, peak, payload, retention, and replicas.
Name ownership and invariants before products.
Separate local transactions from distributed workflows.
Make retries/idempotency/failure visible.
Give a measurement and rollback story.
Use STAR with truthful contribution and evidence.
```
