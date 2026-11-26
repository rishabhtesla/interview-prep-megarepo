# Resilience, observability, configuration, and delivery

## Mental model

Reliability is a bounded contract across time, capacity, and failure. A
timeout limits waiting; a retry repeats work; a circuit breaker stops sending
work; a bulkhead limits blast radius; telemetry makes the result diagnosable.

```text
request
  -> correlation/trace context
  -> timeout budget
  -> bulkhead/concurrency limit
  -> circuit breaker
  -> bounded retry with jitter
  -> dependency
  -> metric + structured log + span
```

## Interview questions and answers

### How does a circuit breaker work?

**Answer:** Closed sends calls and records failures. Open rejects calls
immediately for a cool-down period. Half-open permits a small probe; success
closes the breaker and failure reopens it. Define which errors count, the
window/threshold, cool-down, slow-call policy, and fallback semantics.

```text
Closed --failure threshold--> Open
Open --wait duration--------> Half-open
Half-open --success---------> Closed
Half-open --failure---------> Open
```

Resilience4j provides circuit breaker, retry, rate limiter, time limiter, and
bulkhead modules. A fallback must preserve failure meaning; returning an empty
success list for a failed dependency is dangerous.

### Why do naive retries create storms?

**Answer:** Every caller retries the same overloaded dependency at the same
time, multiplying load and extending recovery time. Use bounded attempts,
exponential backoff, jitter, retryable-error classification, and an overall
deadline. Do not retry validation errors, authorization failures, or non-
idempotent operations without an idempotency design.

### What are the three observability pillars?

**Answer:** Logs describe individual events, metrics aggregate behavior over
time, and traces connect spans across one request. Propagate `traceId` and
`spanId` through HTTP/message headers, keep structured fields bounded, and
never put secrets or high-cardinality user data into metric labels.

### What is a service-level objective?

**Answer:** An SLI is a measurement such as successful request proportion or
latency percentile. An SLO is the target over a window. An error budget is the
allowed failure that guides release/risk decisions. “The service is fast” is
not an SLO until the population, percentile, window, and success definition are
specified.

### How should configuration and secret rotation work?

**Answer:** Package safe defaults, override environment-specific values through
deployment configuration, and load secrets from a secret manager or platform
secret reference. Rotate by versioning/overlap: publish the new secret, allow
readers to accept both during transition, switch writers, then revoke the old
one. Do not log resolved secrets.

### Blue-green, canary, or rolling deployment?

**Answer:** Blue-green keeps two environments and switches traffic, making
rollback simple but doubling capacity. Canary sends a small percentage to the
new version and observes metrics before expanding. Rolling replaces instances
gradually and is efficient but needs backward-compatible APIs/data migrations.
All require health/readiness, rollback criteria, and schema compatibility.

### What is a service mesh?

**Answer:** A sidecar or node-level proxy handles mTLS, routing, retries, and
telemetry outside application code. It can standardize policy, but adds hops,
resource cost, debugging layers, and another control plane. It does not remove
the need for domain idempotency or correct timeout budgets.

### How do you evolve APIs and event schemas safely?

**Answer:** Prefer additive, backward-compatible changes first: add optional
fields, keep old readers working, deploy readers before writers, migrate
traffic, and remove old fields only after the compatibility window. For Avro or
Protobuf, define backward/forward compatibility rules in a schema registry or
build check. For HTTP, version behavior only when compatibility cannot be
preserved; a URL version alone does not solve data migration.

### How do you centralize configuration safely?

**Answer:** Spring Cloud Config, Vault, Kubernetes ConfigMaps/Secrets, or a
cloud secret manager can provide environment-specific values. The client must
define startup versus refresh behavior, stale-config risk, authentication,
audit, and rotation. A configuration server is not a secret store unless it
has the required encryption, access controls, and lifecycle.

## Configuration examples

Illustrative Resilience4j configuration:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      catalog:
        slidingWindowSize: 20
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  retry:
    instances:
      catalog:
        maxAttempts: 3
        waitDuration: 100ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

The numbers are placeholders. Set them from dependency SLOs and measured
latency, then test overload and partial failure. The Spring course's
`CatalogClient` already demonstrates explicit connect/read timeouts and
`503` mapping without an unsafe automatic retry.

## Code and pipeline map

- Logs/health/API behavior: `04-backend-spring-microservices/*/src/main/resources/application.properties`
- Docker: `10-storage-cloud-devops/docker/Dockerfile`
- Kubernetes probes and rollout shape: `10-storage-cloud-devops/k8s/lesson.yaml`
- CI/CD gates: `10-storage-cloud-devops/.gitlab-ci.yml`
- Tests and coverage: Spring `src/test`, Java course executable checks,
  React Vitest tests, and Python `unittest`

## Failure exercise

Stop `catalog-service` while saving progress. Record the trace/correlation ID,
dependency timeout, status mapping, retry count, and whether a database write
occurred. Then decide which layer should own the retry. A retry in both gateway
and client can multiply attempts.

## Revision card

```text
Timeouts bound waiting; breakers bound repeated pressure.
Retries need idempotency, classification, backoff, jitter, and a deadline.
Bulkheads limit blast radius; rate limits protect capacity.
Logs explain events, metrics show trends, traces connect boundaries.
Config is overridable; secrets are referenced, rotated, and never logged.
Deployments need compatibility, health, observability, and rollback criteria.
```
