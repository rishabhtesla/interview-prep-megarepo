# Spring Boot and backend zero-to-hero masterclass

This is the beginner-first route through the Spring track. It starts with one
small service whose entire request path fits on one screen, then grows into the
existing JPA-backed catalog/progress pair and its inter-service failure lab.

The study loop is:

```text
understand -> run -> inspect -> change -> test -> explain
```

## 0. Setup

Use JDK 17 and Maven 3.9+:

```bash
cd 04-backend-spring-microservices
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -version
mvn -pl zero-to-hero-service test
```

The parent build contains three applications. The beginner service is
`zero-to-hero-service`; the existing `catalog-service` and `progress-service`
are the intermediate distributed course.

## 1. Run the first service

Start it in one terminal:

```bash
mvn -pl zero-to-hero-service spring-boot:run
```

Use a second terminal:

```bash
curl -fsS http://127.0.0.1:8083/actuator/health
curl -i -X POST http://127.0.0.1:8083/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"item":"Java course","quantity":2,"unitPriceCents":1500}'
curl -fsS http://127.0.0.1:8083/api/orders
curl -fsS http://127.0.0.1:8083/api/orders/1
```

The valid response is `201 Created`, includes a `Location` header, and has
`totalCents: 3000`. Test the failure contract:

```bash
curl -i -X POST http://127.0.0.1:8083/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"item":"","quantity":0,"unitPriceCents":-1}'
curl -i http://127.0.0.1:8083/api/orders/999
```

The first returns `400` and the second returns `404`; neither is converted into
an empty success response.

## 2. Trace one request

Read the beginner files in this order:

| File | Action | Question to answer |
|---|---|---|
| `FoundationApplication` | Starts component scanning and the embedded server | Which package is scanned, and why are the beans found? |
| `OrderController` | Maps HTTP to Java methods | What does `@Valid` do before `OrderService.create` runs? |
| `OrderRequest` | Defines the input contract | Which invalid values are rejected at the boundary? |
| `OrderService` | Contains the use-case calculation | Why is total calculation not in the controller? |
| `OrderStore` | Owns storage operations | What changes when this becomes a JPA repository? |
| `OrderResponse` | Defines the output contract | Why not expose the internal domain object directly? |
| `ApiErrorHandler` | Maps failures to HTTP Problem Details | How does an exception become a status and JSON body? |

Draw this flow before reading details:

```text
HTTP JSON
  -> DispatcherServlet
  -> OrderController
  -> Bean Validation
  -> OrderService
  -> OrderStore
  -> OrderResponse
  -> JSON + HTTP status
```

## 3. Change exercises

Make one change at a time, add or update a test, and run
`mvn -pl zero-to-hero-service test`.

1. Add a `currency` field and require it to be `USD`.
2. Add `GET /api/orders` filtering by status.
3. Replace the in-memory store with a JPA entity and H2, then explain the
   persistence context and transaction boundary.
4. Add a service-level unit test with a mocked `OrderStore`.
5. Add a global correlation ID and include it in error logs/responses.
6. Make `POST` idempotent with an idempotency key and explain where the key is
   stored and how concurrent duplicates are handled.

The existing service intentionally uses `Math.multiplyExact`, so an integer
overflow is a visible failure instead of silently returning a wrong total. Add a
test for the boundary before changing the numeric type.

## 4. Move to the existing distributed course

After the beginner service, read the existing modules:

| Capability | Where to study | Interview checkpoint |
|---|---|---|
| Constructor injection and configuration | `catalog-service` | Why prefer constructor injection? |
| DTO validation and HTTP contracts | `TopicController`, `ProgressController` | Which layer owns status mapping? |
| JPA/Hibernate and transactions | `Topic`, `StudyProgress`, `ProgressStore` | What happens during dirty checking and commit? |
| Optimistic locking | `StudyProgress` and tests | How is a lost update detected? |
| HTTP client boundary | `CatalogClient` | Why is a remote call not part of the local DB transaction? |
| 404/503 failure mapping | `INTERSERVICE_COMMUNICATION.md` | Which failures are client errors versus dependency failures? |
| Idempotency and tests | `EXERCISES.md`, `INTERVIEW.md` | What happens when the response is lost after a write? |

Run the complete suite:

```bash
mvn clean verify
```

Then follow [INTERSERVICE_COMMUNICATION.md](INTERSERVICE_COMMUNICATION.md) and
perform the stop-catalog failure drill from the main
[learning guide](LEARNING_GUIDE.md).

## 5. Revision card

```text
component scanning
constructor injection
controller vs service vs repository
DTO/input validation vs domain validation
HTTP 2xx/4xx/5xx contract
Problem Details error response
singleton bean lifecycle
configuration and profiles
JPA entity vs immutable DTO
persistence context and dirty checking
transaction boundary and isolation
optimistic locking
local transaction vs distributed operation
timeout vs retry vs circuit breaker
idempotency vs exactly once
health/readiness vs business endpoint
unit test vs MockMvc vs integration test
```

## 6. Completion checkpoint

You are ready for messaging and frontend integration when you can implement a
validated endpoint, explain every layer it crosses, write tests for success and
failure, describe where persistence commits, and predict what happens when a
downstream service is unavailable.
