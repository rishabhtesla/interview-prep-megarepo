# Enterprise integration: Camel, SOAP, and service discovery

This track makes three common enterprise boundaries concrete:

```text
Apache Camel route -> normalize/validate/route a message
SOAP envelope      -> parse a typed XML boundary safely
Eureka-style lookup -> resolve a service instance before an HTTP call
```

It uses Apache Camel's local `direct:` and `mock:` endpoints, so the route can
run without a broker or partner system. The discovery example is an in-memory
model of client-side lookup; it is not a Eureka server.

## Setup

Requirements: JDK 17 and Maven 3.9+. The lesson pins Camel 2.20.2 so the
dependency graph remains reproducible in this small example; production
projects should choose a currently supported Camel line and verify its Java
compatibility.

```bash
cd 12-enterprise-integration
./run.sh
```

The command runs JUnit tests and the executable lesson.

## Lesson 1 — Enterprise Integration Patterns

Source: [`src/main/java/com/interviewprep/integration/CamelRouteLesson.java`](src/main/java/com/interviewprep/integration/CamelRouteLesson.java).

The route performs:

```text
direct:orders
  -> content choice
  -> reject invalid message
  -> normalize valid message
  -> mock endpoint
```

Camel actions:

1. `from("direct:orders")` declares the input endpoint.
2. `choice()` branches on message content.
3. `process(...)` applies a transformation.
4. `to("mock:normalized")` hands the result to an observable endpoint.
5. The test sends messages through `ProducerTemplate` and asserts both branches.

Run only the tests:

```bash
mvn test
```

The `mock:` endpoint is useful in tests because it records exchanges without
requiring a real downstream system. In production, replace it with an HTTP,
JMS, Kafka, file, or other component and define timeout, retry, ordering, and
error-channel behavior explicitly.

## Lesson 2 — SOAP boundary

Source: [`src/main/java/com/interviewprep/integration/SoapEnvelopeLesson.java`](src/main/java/com/interviewprep/integration/SoapEnvelopeLesson.java).

SOAP is an XML envelope plus a body containing an operation. The example:

1. Parses a known namespace-aware envelope.
2. Finds the SOAP `Body`.
3. Reads the partner operation.
4. Rejects malformed or unexpected XML instead of returning an empty success.

The parser disables external entity resolution. XML from a partner is
untrusted input; do not enable DTD/network resolution casually.

This is the message-boundary lesson, not a complete WSDL-generated client.
Production SOAP integration additionally requires WSDL/XSD contracts, generated
types, WS-Security/TLS, timeouts, retries appropriate to the operation, and
contract tests against partner fixtures.

## Lesson 3 — Eureka-style service discovery

Source: [`src/main/java/com/interviewprep/integration/ServiceDiscoveryLesson.java`](src/main/java/com/interviewprep/integration/ServiceDiscoveryLesson.java).

The client:

```text
logical service name -> registry instances -> choose healthy instance -> HTTP call
```

Round-robin selection spreads calls but does not prove that the selected
instance is healthy. A real Spring Cloud Eureka client also needs registration
heartbeats, lease expiry, metadata, zone/region strategy, timeouts, and
fallback behavior. Discovery is not authorization and is not a replacement for
service-level observability.

## Failure exercises

1. Change the Camel predicate so an invalid order is accidentally normalized.
   Add a test that catches the regression.
2. Remove the SOAP namespace from the fixture. Observe the explicit failure.
3. Add a third service instance and verify the selection sequence.
4. Stop a resolved HTTP instance in a real client and explain the timeout,
   retry, and registry-refresh boundaries separately.

## Interview questions

1. What is an Enterprise Integration Pattern?
2. Why put a validation/normalization boundary before a downstream call?
3. How do Camel routes handle errors and retries?
4. Why must XML parsers disable external entities?
5. SOAP versus REST: what contract and operational trade-offs differ?
6. What does Eureka solve, and what does it not solve?
7. Why is client-side load balancing not the same as a circuit breaker?
8. How do you test a route without starting every downstream dependency?

## Revision card

```text
route = message flow plus explicit decisions
mock endpoint = observable test boundary
SOAP = namespace-aware XML contract, not arbitrary strings
untrusted XML needs hardened parser settings
discovery resolves instances; it does not guarantee health or authorization
timeouts and retries belong to a defined failure policy
```
