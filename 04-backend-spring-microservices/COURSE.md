# Spring and microservices: a connected course

Use the running study planner for the first nine lessons. Lessons 10–13 are
**design studies**, not hidden implementations. Each lesson ends with an
observable checkpoint; use [exercises](EXERCISES.md) to turn concepts into changes.

## 1. From Java objects to a Spring application

Without a container you could wire dependencies manually:

```java
var service = new ProgressService(catalogClient, progressStore);
```

Spring performs this composition at startup. `@SpringBootApplication` combines
configuration, auto-configuration, and component scanning under its package.
Our applications live above their controller/service/repository classes, so the
scan sees only the intended service module.

`@RestController` is a component whose return values are serialized as response
bodies. `@Service` describes application logic. Spring Data creates a runtime
repository implementation. Constructor injection makes required dependencies
explicit, allows immutable fields, and enables unit tests without Spring.
One constructor needs no `@Autowired`.

Boot auto-configuration reacts to the classpath and configuration: the web
starter supplies embedded Tomcat and MVC; JPA supplies Hibernate and a datasource.
“Starter” means dependency grouping; “auto-configuration” means conditional bean
definitions. Neither removes the need to understand the lifecycle.

### Bean lifecycle

1. Read definitions and resolve constructor dependencies.
2. Instantiate the object and populate properties.
3. Invoke awareness/post-processors and initialization callbacks.
4. A bean post-processor may return an AOP proxy around the target.
5. Publish the managed singleton; invoke destruction callbacks on shutdown.

Singleton is **per Spring application context**, not a JVM-wide global and not
automatically thread-safe. Controller fields must not hold mutable per-request
notes. Our services hold stateless dependencies; request DTOs are local values.
`CommandLineRunner` seeds catalog after context initialization; its transactional
public `run` method is invoked through the managed bean.

Circular dependencies often reveal confused responsibilities; constructor cycles
fail early. Prefer extracting a collaboration instead of sprinkling `@Lazy`.

**Checkpoint:** in `ProgressServiceTest`, construct the service with mocks. Why
does no Boot context load? Which object creates repositories in integration tests?

## 2. HTTP request flow, DTOs, and validation

```text
Tomcat thread → DispatcherServlet → handler mapping → JSON conversion
  → Bean Validation / method validation → controller → service
  → response DTO → Jackson JSON → status + headers + body
```

The controller owns HTTP concerns, not SQL or remote orchestration. The service
owns the use case. Persistence entities own relational state; response records
define the external contract.

Returning a JPA entity directly can leak internal fields and trigger lazy SQL
during serialization. Separate DTOs let the DB evolve without changing every
client. This demo maps simple fields inside a transaction and disables
Open Session in View (`spring.jpa.open-in-view=false`).

`ProgressRequest` uses `Boolean`, not primitive `boolean`: absent JSON must not
silently become false. `@NotNull` rejects absence/null; `@Size` bounds notes.
Path constraints use Spring MVC's built-in method validation. Do not add
class-level `@Validated` indiscriminately: it can switch to an AOP validation path
with different exception handling.

Validation has layers:

- Syntax/shape: request DTO, path/query constraints → 400.
- Business existence: catalog client verifies topic → 404 or 503.
- Relational invariants: primary key, non-null, version → DB constraint/conflict.

Neither browser `maxLength` nor annotations alone protect the database.
Bean Validation doesn't prove a remote topic still exists at commit time.
The catalog has no deletion API here; if added, define a cross-service deletion
policy rather than pretending an HTTP check is a distributed foreign key.

Spring's `ResponseEntityExceptionHandler` and `ResponseStatusException` produce
ProblemDetail. A status is machine-readable; a human detail should be useful and
must not expose SQL, tokens, or exception stacks.

**Checkpoint:** send `{}` and then `{"completed":false}`. Explain why one is 400
and the other valid, even though both could map to false with a primitive.

## 3. Proxies and transactions: where the boundary really is

Declarative transactions are usually implemented by a proxy:

```text
caller → transactional proxy → begin/join transaction
       → target method → commit or rollback → caller
```

Calling `this.replace(...)` inside the target bypasses that proxy. Merely adding
`@Transactional` to an internal/private method does not create the boundary you
intend. Public methods called from a different managed bean are the simple,
predictable case taught here.

Our actual path is:

```text
ProgressController
  → ProgressService.replace          (no DB transaction)
    → CatalogClient.requireTopic     (bounded HTTP call)
    → ProgressStore proxy
      → ProgressStore.replace        (@Transactional, local H2)
```

Why two services? Holding a DB connection/transaction while a network dependency
stalls extends locks and pool occupancy. Validate remotely first, then perform a
short local transaction. This improves resource use, but **does not** make the
remote check and local commit atomic.

Default propagation `REQUIRED` joins an existing transaction or opens one.
`REQUIRES_NEW` opens an independent transaction, typically suspending the outer
one and consuming another connection. It is not “stronger rollback.” An inner
audit write can survive an outer rollback, which may or may not be intended.
`NESTED` uses savepoint semantics when supported; it is not a portable substitute
for distributed transactions.

Runtime exceptions/errors roll back by default; checked exceptions generally
need `rollbackFor` if rollback is desired. Catching an exception and returning
“success” can defeat the intended semantics, though a transaction already marked
rollback-only still cannot commit. `readOnly=true` is an optimization hint, not a
universal write prohibition.

Commit can fail **after the method body returns** but before the proxy returns to
the caller. `saveAndFlush` sends SQL earlier; **flush is not commit**.

**Checkpoint:** point to the exact proxy crossing in this code. Predict what
happens if remote validation throws. Confirm `ProgressStore` isn't called.

## 4. JPA persistence context, entity states, and query costs

JPA defines an API; Hibernate implements it; Spring Data offers repository
abstractions. H2 is the database, not an ORM.

Entity states: transient (new, unmanaged), managed (tracked in a persistence
context), detached (no longer tracked), removed (scheduled for deletion).
A transaction-scoped persistence context tracks identity and snapshots. Mutating
a managed entity can generate SQL at flush via dirty checking. `save` is not
required for every managed property assignment, but explicit save here keeps
new/existing replacement paths uniform.

`StudyProgress` has an assigned string ID and nullable `@Version Long`. Spring
Data can identify a new versioned entity using its null version and persist it.
A version is included in update predicates, allowing overlapping updates to be
detected when the expected version no longer matches.

```sql
-- Conceptual SQL, actual names and bind syntax vary:
update study_progress
set completed = ?, note = ?, version = version + 1
where topic_id = ? and version = ?;
```

Zero matching rows indicates a concurrency conflict. This demo maps recognized
optimistic locking/constraint exceptions to 409. It does not supply client-visible
ETags or resolve conflicting notes.

### N+1 queries

This model has no entity relationships, so it deliberately has **no N+1 example
running in production code**. If Topic had a lazy collection of Lessons:

```java
for (Topic topic : repository.findAll()) {
    topic.getLessons().size(); // potentially one extra SELECT per topic
}
```

One parent query plus N child queries can dominate latency. Mitigations depend
on the query: DTO projection for required fields, fetch join, entity graph, or
batch fetching. Eager loading everywhere often overfetches and does not guarantee
a single efficient query. Collection fetch joins plus offset pagination can
duplicate parent rows or cause in-memory pagination; use a page of IDs then fetch
details, or a purpose-built projection.

Indexes accelerate access at write/storage cost. Our primary key already indexes
topic ID. At large scale, filtering/sorting by track and ID suggests a composite
index `(track, id)`; verify with the target database's EXPLAIN plan.

**Checkpoint:** enable `spring.jpa.show-sql=true` temporarily via command-line
configuration, PUT twice, and distinguish SELECT, INSERT/UPDATE, flush, and commit.
Don't log sensitive binds in a real environment.

## 5. Isolation and concurrent writers

ACID: atomicity applies within one DB transaction; consistency means declared
invariants are preserved; isolation describes concurrent visibility; durability
depends on the database/storage guarantees. A successful response isn't proof
that your disk/backups can survive every fault.

| Phenomenon | Example | Relevant protection |
|---|---|---|
| Dirty read | B sees A's uncommitted completion | READ COMMITTED usually prevents |
| Non-repeatable read | Same row changes between two reads | Repeatable read semantics, DB-specific |
| Phantom | Re-running a predicate sees newly inserted rows | Serializable/predicate locking, DB-specific |
| Lost update | Two writers derive updates from the same version | Optimistic version or appropriate locking |
| Write skew | Two transactions update different rows violating a cross-row rule | Serializable or explicitly locked invariant |

Isolation names differ in implementation between databases. H2's behavior is
not proof PostgreSQL/MySQL will behave identically. Use the eventual production
DB in integration tests before promising those semantics.

Optimistic locking fits low-contention editing: read version, update with that
version, reject a collision. Pessimistic locks serialize contested access but can
increase waits/deadlocks. Retries must be bounded and re-read/recompute the
business decision; blindly retrying arbitrary side effects duplicates work.

For this single-learner planner, sequential PUTs are last-writer-wins. Two
overlapping DB transactions can produce 409. An old browser tab saving much
later is not caught because it doesn't submit a version. To reject stale clients,
add ETag/If-Match and specify precondition failure (usually 412).

**Checkpoint:** explain why `@Version` doesn't make this application a
collaborative note editor.

## 6. REST semantics, idempotency, and consistency

GET is safe/read-only in intended semantics. PUT replaces a resource at a known
URI. Repeating PUT should leave the same observable resource state. A
`POST /toggle` changes true→false→true on retries and is not idempotent.
Our PUT sends explicit `completed` and `note`.

Idempotency doesn't mean all internal effects disappear. Logs, counters, and
internal versions may change on repeated requests. If a future “completion”
write sends email on every invocation, resource replacement alone no longer
protects that side effect. Gate transitions or use deduplicated events.

Network ambiguity:

1. Client sends PUT.
2. Server commits.
3. Response is lost.
4. Client times out and cannot distinguish committed from uncommitted.

The UI says to reload. A repeat of the **same state** is safe for current fields;
an automatic toggle would not be. For payment-style commands use an idempotency
key stored atomically with the result, scoped to the caller and operation, with a
request hash and retention policy. Reusing a key with a different payload should
be rejected.

Microservices don't share a transaction just because they share Java. Progress
and catalog own separate data; an HTTP lookup is synchronous validation, not
two-phase commit. Availability trade-off: progress writes fail closed when
catalog cannot validate. Reads continue from progress's local DB.

**Checkpoint:** repeat the same PUT twice and show exactly one progress row.
Then stop catalog and show that even editing existing progress fails visibly.

## 7. HTTP clients and resilience without magical fallbacks

`RestClient` is a synchronous, blocking client. It suits this small MVC service;
each in-flight call occupies a request thread. `WebClient` provides reactive
composition but wrapping blocking JPA in a reactive controller does not
automatically create a non-blocking system.

`CATALOG_BASE_URL` is configuration, never user-supplied request data. Building
outbound URLs from untrusted arbitrary URLs risks SSRF. We expand a validated ID
as a URI path variable.

Failures are classified:

- Catalog 404 → local 404 (topic genuinely unknown).
- HTTP 5xx, timeouts, transport errors, invalid response → 503.
- Local invalid JSON/body → 400 before orchestration.

No empty “successful” fallback is returned. A fallback is defensible only when
its degraded meaning is valid and communicated, e.g. cached catalog data marked
stale for browsing. Claiming “saved” without a write would corrupt user trust.

Timeouts limit waiting phases. Retries consume time and downstream capacity;
three layers each retrying three times can amplify traffic dramatically.
Retry only suitable failures, for safe/idempotent work, with a total budget,
exponential backoff and jitter, and a maximum attempt count.

**Circuit breaker (not implemented):** closed allows calls and measures failures;
open rejects quickly for a interval; half-open probes recovery. It does not
replace timeouts, rate limits, or a bulkhead. A bulkhead bounds concurrent
dependency work so one slow service cannot occupy every thread/connection.
Choose thresholds from measured traffic, not copied defaults.

**Checkpoint:** point to timeout configuration and HTTP response validation.
Explain why progress health may stay UP while saves return 503.

## 8. Testing at meaningful boundaries

| Layer | Here | What it catches / cannot prove |
|---|---|---|
| Unit | `ProgressServiceTest` | Orchestration/order; cannot prove HTTP or SQL |
| HTTP + persistence integration | `CatalogIntegrationTest` | Routing, validation, JSON, H2, seed; not TCP |
| Integration with local downstream HTTP | `ProgressIntegrationTest` | Real HTTP client failure paths + H2 writes |
| End to end | README curl + React startup drill | Real processes/proxies and persistence path |

Use deterministic test inputs, isolated DB profiles, and ephemeral downstream
ports. Tests should assert observable semantics (status/body/database state),
not incidental implementation details. Verify “failure does not write,” not just
“an exception happened.”

MockMvc sends requests through Spring MVC without binding an application port.
`@WebMvcTest` is a narrower MVC slice useful when DB wiring is irrelevant.
`@DataJpaTest` tests repositories/mappings. Full `@SpringBootTest` is more costly
but useful for wiring/configuration. Avoid mocking everything in an “integration”
test and then claiming the database was tested.

A consumer-driven contract can verify the fields progress expects from catalog;
it doesn't replace provider functional tests. H2 tests don't cover migrations,
query plans, locking, or dialect details of another DB. Testcontainers is a
possible future extension, **not configured here**.

**Checkpoint:** inspect the timeout integration test and explain what is real
(HTTP socket, client, MVC, JPA) and what is simulated (catalog responses).

## 9. Observability and operating the demo

Logs describe events; metrics aggregate behavior over time; traces show causal
paths across services. They complement rather than replace one another.
Good request telemetry includes route template, status, duration, service,
trace/request ID, and dependency outcome—without secrets or study-note text.

For this code, Boot supplies startup/request-error logs and Actuator health/info.
Boot also provides instrumentation infrastructure, but **no tracing backend,
Prometheus scrape, dashboards, SLO alerting, or propagated trace setup** is
configured. Don't infer those capabilities from the Actuator dependency.

Production design targets:

- RED: request Rate, Errors, Duration for each service/route.
- USE: resource Utilization, Saturation, Errors for thread/connection pools.
- SLI: fraction of saves successfully completed within a stated latency.
- SLO: a chosen target over a window, with an error budget and alert policy.
- Correlate the browser failure to progress's catalog call using trace context.
- Never use user ID/topic ID as unbounded metric labels.

Liveness asks whether the process should be restarted; readiness asks whether it
can serve the intended traffic. Including every remote dependency in liveness
can create restart cascades. A progress read endpoint can be useful even when
catalog is down; readiness policy may need route-specific thinking.

**Checkpoint:** fetch both health endpoints while catalog is running, then stop
catalog. Does progress health capture the entire user journey? Why not?

## 10. Discovery, gateway, and configuration — design study

At two local services, a configured URL is transparent and sufficient.
At larger scale, stable DNS/service discovery can locate changing instances.
Client-side discovery chooses an instance from a registry; server-side discovery
lets a load balancer/service name route to instances. Kubernetes Services often
remove the need for an application-level registry. A registry isn't a gateway.

An API gateway can provide an external routing boundary, TLS termination,
rate limits, request size limits, and authentication integration. Business
authorization still belongs where resources are accessed. Vite's development
proxy is only a local convenience, **not** a production gateway implementation.

Configuration separates deployment choices from binaries. Here Spring
properties + environment supply the catalog URL and local profile. A config
server can centralize non-secret values and versioning; a secret manager manages
credential lifecycle and access. Dynamic refresh needs careful consistency and
rollback design; not every connection pool safely reconfigures mid-request.
Do not commit production secrets or expose `/actuator/env`.

Proposed deployment:

```text
Browser → TLS edge/gateway → catalog replicas → catalog DB
                          → progress replicas → progress DB
                                        └──→ catalog service DNS
           secret manager/config → service startup
```

Stateless app replicas require external databases rather than shared writable
H2 files. Define startup/readiness checks, shutdown grace, resource limits,
schema migration ownership, rollback-compatible API/DB changes, and backups.
Containers do not supply these guarantees automatically.

**Design question:** which component owns the study data? Which configuration
changes between local and staging? Which secret must never go into a Vite bundle?

## 11. Eventual consistency and transactional outbox — design study

Suppose completing a topic should update recommendations asynchronously.
Naive sequence `commit progress; publish event` loses the event if the process
crashes between steps. Reversing the sequence risks publishing an event about a
write that rolls back. A local `@Transactional` does not cover an arbitrary broker.

Transactional outbox proposal:

```text
one progress DB transaction:
  update progress
  insert outbox(eventId, topicId, version, type, payload)
commit
relay reads committed outbox → broker → recommendation consumer
```

The relay can crash after publish before marking sent, so duplicates remain
possible. Use at-least-once delivery with idempotent consumers: insert processed
event ID with a unique constraint and apply the effect in the **same consumer DB
transaction**. Ordering matters per aggregate; track sequence/version, partition
by the relevant key, and decide how to handle late events. Event schema evolution,
retention, poison messages, replay, and dead-letter recovery need policy.

“Eventually consistent” requires a convergence mechanism and observable lag,
not an excuse for indefinite incorrectness. Communicate pending state in the UI.
There is no outbox table, relay, or broker in the shipped implementation.

**Design checkpoint:** enumerate the crash points before/after commit, publish,
acknowledgment, and consumer commit. State why “exactly once” is scoped, not a
universal end-to-end promise.

## 12. Sagas and distributed workflows — design study

The planner currently needs **no saga**. For a hypothetical paid tutoring
workflow, steps might reserve a slot, authorize payment, and enroll a learner.
Each step is a local transaction; failure triggers compensating actions, such as
release reservation or void authorization.

Compensation is a business action, not a database rollback across services.
A delivered email cannot be “unseen”; a refund may incur fees and fail itself.
Store workflow state, correlation IDs, deadlines, retry policy, and compensation
status. Make commands idempotent and reconcile stuck workflows.

Choreography distributes event reactions but makes global flow harder to inspect.
Orchestration centralizes workflow coordination but needs its own availability,
state, and versioning design. Neither eliminates partial failure.

**Design checkpoint:** write a state machine with `RESERVED`, `AUTHORIZED`,
`ENROLLED`, `COMPENSATING`, `FAILED_REQUIRES_REVIEW`. Which transitions are legal?
What happens when the “void authorization” call times out?

## 13. Authentication, authorization, and secure deployment — design study

Authentication identifies a caller; authorization decides whether that caller may
access this record. CORS is a browser cross-origin policy, not either of those.

A future multi-user system should derive learner identity from a trusted
authenticated principal, never blindly trust a body `userId`. Every progress
query/write must constrain ownership. Add schema uniqueness `(learner_id, topic_id)`
and tests proving learner A cannot read or edit learner B.

For OAuth2/OIDC, distinguish authorization server from resource server. Resource
servers validate signature, allowed algorithm, issuer, audience, expiry and relevant
claims; reading a JWT payload is not verification. Key rotation and clock skew
need bounded policies. JWTs are typically signed, not encrypted: don't place secrets
inside claims. OAuth2 delegates authorization; OIDC adds identity semantics.

Browser session design has trade-offs. HttpOnly/Secure/SameSite cookies reduce
script token exposure but cookie-authenticated mutations need CSRF defenses.
JavaScript-accessible bearer tokens are vulnerable to XSS exfiltration. A BFF can
keep tokens server-side, at operational cost. Don't add a hardcoded JWT secret
and call this demo production-ready.

Production checklist: TLS, authorization tests, appropriate CSRF/CORS, dependency
maintenance, parameterized queries, server validation, least-privilege DB roles,
secret storage/rotation, audit policy, rate limits, data retention/deletion,
backups/restore drills, migration testing, and incident response.

**Final checkpoint:** present what exists versus what is proposed in two columns.
A strong interview answer is honest about the boundary.
