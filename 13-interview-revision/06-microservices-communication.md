# Microservices, decomposition, discovery, and communication

## Mental model

Microservices are independently deployable ownership boundaries, not merely
small classes or separate Maven modules. The boundary should contain a cohesive
business capability, data ownership, API contract, operational responsibility,
and failure policy.

```text
client
  -> gateway/BFF (optional)
  -> service A
       -> local transaction + service A database
       -> HTTP/gRPC/event boundary
  -> service B
       -> service B database
```

## Run the repository flow

From `04-backend-spring-microservices`:

```bash
mvn clean verify
```

Start catalog-service on `8081`, progress-service on `8082`, and the React
client on `5173`. Progress validates a topic through `CatalogClient` before
writing its own database. Read
[`INTERSERVICE_COMMUNICATION.md`](../04-backend-spring-microservices/INTERSERVICE_COMMUNICATION.md)
and inspect:

```text
progress-service/ProgressController
  -> ProgressService
  -> CatalogClient (remote precondition)
  -> ProgressStore (local transaction)
```

## Interview questions and answers

### Monolith, modular monolith, or microservices?

**Answer:** A monolith deploys one unit; a modular monolith keeps strong
in-process boundaries; microservices deploy and scale boundaries independently.
Start with a modular monolith when the domain/team/operational load does not
justify distributed complexity. Do not choose microservices to solve unclear
ownership, slow tests, or poor code organization.

### How do you decompose a monolith?

**Answer:** Identify business capabilities and bounded contexts, map ownership
and invariants, find a stable seam, extract an anti-corruption layer, and use
the Strangler Fig pattern to route one capability at a time. Measure latency,
failure rate, deployment independence, and operational cost after every
extraction.

### Synchronous versus asynchronous communication?

**Answer:** REST/gRPC is useful when the caller needs an immediate response and
the dependency is part of the request contract. Events/queues decouple timing,
allow replay or buffering, and introduce eventual consistency, duplicate
delivery, schema evolution, and observability requirements. Choose from
business latency and failure needs, not fashion.

### What belongs in an API gateway?

**Answer:** Edge routing, TLS termination, coarse rate limiting, request
authentication, protocol translation, and correlation propagation can belong at
the gateway. Business authorization, invariants, resource ownership, and
domain-specific validation remain in services. A gateway must not become a
distributed monolith.

### BFF versus one unified gateway?

**Answer:** A Backend for Frontend gives one client type an API shaped for its
screen/workflow and can reduce over-fetching. A shared gateway centralizes
cross-cutting edge concerns. BFFs add deployments and ownership but avoid
forcing mobile, web, and partner clients into one compromise contract.

### How does service discovery work?

**Answer:** Client-side discovery asks a registry for instances and selects one;
server-side discovery sends the request to a load balancer that selects an
instance. Eureka/Consul are registry models; Kubernetes Services/DNS are
platform discovery. Discovery answers “where might an instance be?”—health
checks, timeouts, authentication, and circuit breaking remain separate.

### How do services authenticate each other?

**Answer:** Use mTLS for workload identity, OAuth2 client credentials for
service tokens, or a carefully validated JWT propagation model. Validate issuer,
audience, expiry, scopes, and service identity; do not trust a forwarded user
header without a trusted boundary.

## Configuration walkthrough

The local progress service makes the remote boundary explicit:

```properties
catalog.base-url=${CATALOG_BASE_URL:http://127.0.0.1:8081}
catalog.connect-timeout=1s
catalog.read-timeout=2s
server.port=8082
```

The environment variable changes the endpoint without rebuilding the artifact.
In production, service identity, TLS, discovery, and secret references belong
in deployment configuration, not hard-coded URLs.

## Failure lab

1. Stop catalog-service.
2. Save an existing progress item.
3. Expect `503`, no local write, and a draft that remains available to retry.
4. Send a missing topic ID and expect `404`, not a newly created topic.
5. Restart catalog and retry the explicit PUT.

The remote call and local transaction are not one atomic transaction. A lost
response after a successful write creates ambiguity; use an idempotent command,
reload, and conflict handling.

## Revision card

```text
Bounded context is a business/data ownership boundary.
Microservices trade local simplicity for distributed failure.
Gateway handles edge concerns; services own domain rules.
Discovery locates instances; it does not prove health or authorization.
Every sync call needs timeout, status mapping, and retry policy.
Database-per-service preserves ownership; cross-service reads need a design.
```
