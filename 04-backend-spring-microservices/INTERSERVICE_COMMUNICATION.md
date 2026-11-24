# Inter-service communication: a step-by-step Spring lesson

This lesson explains how one backend service calls another service. It uses the
two services in this directory:

```text
browser / curl
       |
       v
progress-service :8082
       |
       | GET /api/topics/{topicId}
       v
catalog-service  :8081
       |
       v
catalog database
```

The progress service then writes its **own** database only after the catalog
confirms that the topic exists. This is a local learning example of synchronous
HTTP communication. It is not a distributed transaction.

## 1. What problem are we solving?

The progress service stores a user's completion state. It must not store
progress for an unknown topic. The catalog service owns topic existence, so the
progress service asks the catalog before writing.

This demonstrates a common microservice rule:

> A service owns its data. Another service cannot safely read its database
> directly; it uses a documented API or an event contract.

## 2. Vocabulary

| Term | Meaning in this lesson |
|---|---|
| Caller | `progress-service`, which sends the request |
| Downstream | `catalog-service`, which receives the request |
| Synchronous call | Caller waits for a response before continuing |
| DTO | Data-transfer object used at the API boundary |
| Timeout | Maximum time allowed for a network operation |
| 404 | The catalog does not know the requested topic |
| 503 | The catalog cannot currently serve the request |
| Idempotent PUT | Repeating the same replacement request has the same state effect |
| Correlation ID | An identifier carried through logs for one request |
| Distributed transaction | A protocol coordinating multiple resource owners; not used here |

## 3. Synchronous versus asynchronous communication

### Synchronous HTTP

```text
request -> service A -> waits for service B -> response
```

Use it when the caller needs an immediate answer, such as validating a topic
before saving progress.

Advantages:

- Simple request/response mental model.
- Immediate validation result.
- Easy to call from a browser or `curl`.

Costs:

- A is coupled to B's availability and latency.
- A request thread may wait.
- Timeouts, retries, and partial failure need explicit handling.

### Asynchronous messaging

```text
producer -> durable queue/topic -> consumer
```

Use it when work can happen later, when the producer should be decoupled from
consumer availability, or when many consumers need the same event. Kafka and
RabbitMQ lessons should be studied after this HTTP lesson.

Asynchronous messaging changes the questions: delivery guarantee, ordering,
duplicate messages, acknowledgement, retry, dead-letter handling, and eventual
consistency.

## 4. The request contract

The browser sends this to progress-service:

```http
PUT /api/progress/react-effects
Content-Type: application/json

{"completed":true,"note":"Cleanup cancels obsolete reads"}
```

The progress service sends this to catalog-service:

```http
GET /api/topics/react-effects
```

If successful, progress-service writes its local database and returns:

```json
{
  "topicId": "react-effects",
  "completed": true,
  "note": "Cleanup cancels obsolete reads"
}
```

The important ownership rule is:

```text
catalog-service owns whether "react-effects" exists
progress-service owns whether it is completed
```

## 5. Start the actual services

From `04-backend-spring-microservices`:

```bash
mvn clean verify
```

Terminal 1:

```bash
cd catalog-service
java -jar target/catalog-service-1.0.0-SNAPSHOT.jar
```

Terminal 2:

```bash
cd progress-service
java -jar target/progress-service-1.0.0-SNAPSHOT.jar
```

Verify each service separately:

```bash
curl -fsS http://127.0.0.1:8081/actuator/health
curl -fsS http://127.0.0.1:8082/actuator/health
```

Expected result from both:

```json
{"status":"UP"}
```

Now call the downstream service directly:

```bash
curl -fsS http://127.0.0.1:8081/api/topics/react-effects
```

Then call the caller service:

```bash
curl -i -X PUT http://127.0.0.1:8082/api/progress/react-effects \
  -H 'Content-Type: application/json' \
  -d '{"completed":true,"note":"Cleanup cancels obsolete reads"}'
```

Finally read progress:

```bash
curl -fsS http://127.0.0.1:8082/api/progress
```

## 6. Read the code in execution order

### Step 1: HTTP route accepts the request

Source: `progress-service/src/main/java/com/interviewprep/progress/ProgressController.java`

```java
@PutMapping("/{topicId}")
public ProgressResponse replace(
        @PathVariable @Pattern(regexp = "[a-z0-9-]{1,80}") String topicId,
        @Valid @RequestBody ProgressRequest request) {
    return service.replace(topicId, request);
}
```

What each part does:

1. `@PutMapping` maps an HTTP PUT to this method.
2. `{topicId}` extracts the path value.
3. `@Pattern` rejects malformed IDs before business logic.
4. `@Valid` validates the JSON body.
5. The controller delegates to the service; it does not call the database or
   the remote service directly.

### Step 2: service coordinates the use case

Source: `progress-service/src/main/java/com/interviewprep/progress/ProgressService.java`

```java
public ProgressResponse replace(String topicId, ProgressRequest request) {
    catalog.requireTopic(topicId);
    return store.replace(topicId, request);
}
```

Actions:

1. Call the catalog boundary.
2. Stop immediately if the catalog rejects or cannot answer.
3. Call the local persistence boundary only after validation succeeds.
4. Return the canonical saved response.

This ordering is deliberate. The local database transaction is kept short and
is not held open while an HTTP request is waiting.

### Step 3: client performs the remote call

Source: `progress-service/src/main/java/com/interviewprep/progress/CatalogClient.java`

```java
public void requireTopic(String topicId) {
    try {
        var topic = client.get()
                .uri("/api/topics/{id}", topicId)
                .retrieve()
                .body(CatalogTopic.class);
        if (topic == null || !topicId.equals(topic.id())) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catalog returned an invalid response");
        }
    } catch (RestClientResponseException ex) {
        if (ex.getStatusCode().value() == 404) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Topic not found");
        }
        throw unavailable(ex);
    } catch (RestClientException ex) {
        throw unavailable(ex);
    }
}
```

Actions:

1. Build a GET request against the configured catalog base URL.
2. Substitute `topicId` into the URI template.
3. Deserialize JSON into a small response record.
4. Treat a missing or inconsistent body as an unusable downstream response.
5. Translate catalog 404 into a business-level 404.
6. Translate timeout, connection, and other downstream errors into 503.

The caller does not expose a raw stack trace or accidentally claim that the
progress was saved.

### Step 4: local persistence writes

Source: `progress-service/src/main/java/com/interviewprep/progress/ProgressStore.java`

The store owns the local transaction. The remote HTTP call is outside that
transaction:

```text
ProgressController
  -> ProgressService
     -> CatalogClient (remote HTTP)
     -> ProgressStore (local transaction)
        -> ProgressRepository (local database)
```

This is **not atomic across both services**. If the catalog says yes and the
local database fails, the overall operation fails after the remote read. If a
future version publishes an event after the write, it will need an outbox or
another reliable handoff rather than assuming two operations commit together.

## 7. Failure lab

### Unknown topic

```bash
curl -i -X PUT http://127.0.0.1:8082/api/progress/unknown \
  -H 'Content-Type: application/json' \
  -d '{"completed":true,"note":"Should not be written"}'
```

Expected behavior:

```text
catalog-service -> 404
progress-service -> 404
progress database -> unchanged
```

### Catalog unavailable

1. Keep progress-service running.
2. Stop catalog-service.
3. Run the valid PUT again.

Expected behavior:

```text
progress-service -> 503
progress database -> unchanged
caller -> should retain the draft and retry deliberately
```

This is why returning `[]` or a fake success on a failed dependency is
dangerous: it converts an outage into incorrect data.

### Ambiguous timeout

The caller may time out after the server committed. A retry can repeat the
operation. The explicit replacement PUT is idempotent for the same payload,
but the network does not provide an exactly-once guarantee.

## 8. Production concerns to add deliberately

This local example intentionally keeps the first lesson small. In production,
add and document:

1. **Timeouts:** connection and response limits; do not wait forever.
2. **Retry policy:** retry only transient failures, with bounded exponential
   backoff and jitter; do not retry validation errors.
3. **Circuit breaker:** stop repeatedly calling a failing dependency.
4. **Bulkhead:** isolate connection pools and threads between dependencies.
5. **Authentication:** service identity, credentials, or mTLS.
6. **Correlation ID:** propagate a request ID through logs and messages.
7. **Metrics:** latency, timeout count, status counts, retry count, and
   downstream availability.
8. **Contract versioning:** additive changes, compatibility tests, and explicit
   deprecation.
9. **Idempotency:** stable request keys or replacement semantics for retries.
10. **Data consistency:** decide whether the workflow is synchronous, event
    driven, saga-based, or eventually consistent.
11. **Rate limits:** protect the downstream service from a traffic spike.
12. **Secrets:** never put credentials in the React bundle or source code.

## 9. Interview answer

> “For a synchronous dependency, I keep the controller thin and put the call
> in a client boundary. I validate the downstream response, translate 404 to a
> domain-level not-found response, and translate timeout/network/5xx failures
> to 503. I validate remotely before opening the short local transaction, but I
> do not claim that this is a distributed transaction. In production I would
> add bounded retries for transient errors, circuit breaking, correlation IDs,
> metrics, authentication, and an idempotency strategy. If the workflow could
> be asynchronous, I would evaluate Kafka/RabbitMQ and explicitly accept
> eventual consistency.”

## 10. Next exercise

Implement a `CatalogClient` test for each case:

1. Valid topic returns normally.
2. Catalog 404 becomes `NOT_FOUND`.
3. Catalog timeout becomes `SERVICE_UNAVAILABLE`.
4. Invalid response body becomes `SERVICE_UNAVAILABLE`.
5. `ProgressStore.replace` is not called for cases 2–4.

Then compare this synchronous design with an event-driven design where the
catalog publishes `TopicCreated` and progress consumes a local projection.
