# Spring / microservices interview practice — worked answers

For each prompt: answer in 60–90 seconds, give a code-specific example, then name
one trade-off. Follow-up questions intentionally probe beyond definitions.

## 1. What happens when this application starts?

`SpringApplication.run` builds an application context, discovers configuration
and components, applies conditional auto-configuration, constructs dependencies,
wraps relevant beans in proxies, and starts the embedded web server. Boot creates
a datasource/JPA infrastructure because the starters and configuration support it.
The catalog runner inserts missing seed topics. Requests then go through MVC to
controllers. **Follow-up:** a bean initialization failure aborts startup; a bean
existing doesn't mean its downstream HTTP dependency is healthy.

## 2. Why constructor injection instead of field injection?

It makes required dependencies visible and allows final fields and plain Java
unit tests. `new ProgressService(catalog, store)` is enough in the unit test.
Field injection hides requirements until container setup and encourages partially
initialized objects. Constructor injection is not a guarantee of good design:
a constructor with fifteen collaborators can still signal excessive responsibility.

## 3. Is a singleton service thread-safe?

No. Scope controls instances, not synchronization. Many HTTP threads can call the
same service simultaneously. Our service stores only collaborators and uses
method-local request data, while transactions isolate DB work. Storing “current
learner” or a mutable last-note field on the singleton would race. A concurrent map
protects its own operations, not arbitrary multi-step business invariants.

## 4. Why did my `@Transactional` method not start a transaction?

The caller may have bypassed the proxy through self-invocation, constructed the
object with `new`, or invoked a method not interceptable by the configured proxy.
In this code `ProgressService` calls a public method on a separate managed
`ProgressStore`, making interception explicit. Moving annotations around without
tracing the call path is not a diagnosis. Final/private methods also interact
with proxy type limitations.

## 5. Does a transaction roll back every exception?

By default runtime exceptions and errors trigger rollback; checked exceptions
generally do not unless configured. Catching an error can prevent propagation,
but an already rollback-only transaction still fails to commit. I would test the
required behavior and configure `rollbackFor` for checked business failures when
appropriate. External side effects don't roll back with the local DB.

## 6. Why separate orchestration from the transactional store?

The catalog call can take seconds; holding a DB transaction during that call
wastes connection/lock capacity. `ProgressService` validates remotely before
calling transactional `ProgressStore`. The trade-off is a gap between validation
and commit: it is not cross-service atomicity. The current immutable catalog API
makes the gap manageable; adding deletion requires an explicit consistency policy.

## 7. What is the difference between save, flush, and commit?

`save` delegates persistence/merge semantics for an entity. Flush synchronizes
pending changes to SQL and can surface constraints; the transaction can still roll
back. Commit completes the transaction. Managed entity changes are dirty-checked,
so a call to save is not required for every mutation. Our saveAndFlush isn't an
early success guarantee—the proxy's commit can still fail.

## 8. How would you detect and fix N+1?

Measure SQL counts/latency for a representative endpoint, especially inside loops
that access lazy associations. Use a DTO projection, fetch join/entity graph, or
batch fetch based on required data and pagination. Don't set every association
EAGER: that can overfetch without reducing query count. This demo has no
relationships, so claiming it already fixes a real N+1 would be inaccurate.

## 9. What does `@Version` protect here?

It detects overlapping updates that read the same entity version; an update with
an old expected version affects no row and becomes an optimistic-lock failure.
We map recognized conflicts to 409. It doesn't detect a stale browser that sends
no version after another transaction has already committed. Add client ETags and
If-Match if rejecting stale edits is part of the API requirement.

## 10. Which isolation level prevents all application races?

No slogan replaces identifying the invariant. Serializable can prevent anomalies
equivalent to concurrent nonserial execution, but may abort transactions and still
doesn't coordinate arbitrary external side effects. A single-row version check
can protect lost updates efficiently; a cross-row capacity rule may need a
constraint, explicit locking, or serializable retries. Database implementations
of named isolation levels differ.

## 11. Why PUT rather than a toggle endpoint?

PUT sends intended state for a known resource. Repeating
`{"completed":true,"note":"x"}` keeps that state, while repeating a toggle changes
it twice. Our API returns 200 for create and replace by documented choice.
Idempotent resource state doesn't imply exactly-once execution or side effects.
If the response is lost after commit, the caller must reconcile or retry safely.

## 12. How do you map downstream failures?

A catalog 404 means invalid topic, so 404 is appropriate. Catalog 5xx, transport
failure, timeout, or an unusable response means this service cannot validate now,
so 503 preserves that distinction. Invalid client input stays 400. I don't turn
all failures into empty lists or report “saved” without a write; the UI must retain
drafts and show the failure.

## 13. How would you add retries?

First choose a total latency budget and determine which operations are safe to
repeat. Retry selected transient errors with bounded attempts, exponential backoff
and jitter, respecting server retry guidance where relevant. Don't retry malformed
requests or genuine 404s. Avoid nested retries across layers. This demo uses no
automatic retries; the user deliberately retries an explicit-state PUT.

## 14. Circuit breaker versus timeout versus bulkhead?

A timeout limits a call's wait; a breaker stops repeatedly calling a failing
dependency and probes recovery; a bulkhead limits concurrent work/resources so
one dependency doesn't exhaust the application. All address different failure
modes. A breaker needs thresholds and observability; it isn't evidence a fallback
is semantically correct. Only connect/read timeouts are implemented here.

## 15. Is Actuator health enough for an SLO?

No. Health reports selected instantaneous component checks; user-journey success
and latency require request metrics over a defined window. Progress health can
be UP while writes fail because catalog isn't part of that indicator. For an SLO,
define eligible save attempts, success criteria and latency threshold, then
measure good/total events and alert on error-budget burn. Avoid high-cardinality labels.

## 16. How would you guarantee an event accompanies a DB update?

Write the update and an outbox record in one local DB transaction. A relay
publishes committed outbox records. Publishing then crashing before marking sent
can duplicate events, so consumers deduplicate by event ID in the same transaction
as their effect. This gives a durable delivery mechanism with scoped guarantees,
not universal exactly-once processing across every system.

## 17. When do you need a saga?

When a business workflow spans independently committed services and partial
failure needs coordinated recovery. Model steps and compensations explicitly;
they aren't distributed rollback. Choose orchestration for visible centralized
flow or choreography for looser event reactions, acknowledging complexity either
way. The current planner doesn't need a saga; adding one for buzzwords would be overengineering.

## 18. Service discovery, gateway, and config server: same thing?

No. Discovery finds service instances; a gateway routes and mediates external
traffic; configuration supplies environment-specific settings. A Kubernetes
Service DNS name may solve discovery without Eureka. Vite's dev proxy solves
local same-origin development only. Central config doesn't automatically secure
secrets or make runtime refresh safe.

## 19. How would you make this multi-user and authenticated?

Add a trusted authentication flow and resource authorization, derive learner ID
from the principal, key progress by `(learner, topic)`, and constrain every query
and update by ownership. Test cross-user denial. Choose browser session/token
storage with CSRF/XSS trade-offs in mind. CORS isn't authentication, and accepting
a client-supplied learner ID isn't authorization. The shipped demo has none of this.

## 20. What does this project's test suite prove?

It covers orchestration order, HTTP routing/validation/error contracts, basic H2
persistence and idempotent replacement, and real client behavior against a local
HTTP stub including timeout. It doesn't prove production DB isolation, external
identity integration, high load, browser layout, or fault tolerance of a real
cluster. End-to-end startup/reload drills and React tests cover additional layers;
claim precisely which environment and boundaries were exercised.

## 21. Would you choose microservices for twelve topics?

Normally a modular monolith would be simpler: one deploy, local transactions, and
less operational overhead. These services are split to **teach** ownership,
network failure, contracts, and independent processes. Production boundaries
should follow domain/team/scaling needs rather than class names. A strong answer
can justify undoing a split when distributed costs exceed its benefits.
