# Messaging, streaming boundaries, and distributed consistency

## Mental model and flow

Assume at-least-once delivery unless a narrower guarantee is explicitly
defined. A message acknowledgement is not a database commit.

```text
producer
  -> broker/topic/queue
  -> receive
  -> validate schema + authorization
  -> idempotency check
  -> local transaction/side effect
  -> acknowledge/commit offset
  -> retry or dead-letter on failure
```

## Run the local examples

```bash
cd 09-messaging-streaming
./run.sh
```

Read `MessageSemantics.java` first, then `BrokerSemantics.java` and
`OutboxInbox.java`. The local programs use Java 17 and the standard library, so
they demonstrate delivery and failure semantics without pretending to be a
Kafka/RabbitMQ cluster.

## Interview questions and answers

### At-most-once, at-least-once, or exactly-once?

**Answer:** At-most-once acknowledges before processing and can lose work.
At-least-once acknowledges after processing and can duplicate work.
Exactly-once is only meaningful inside a defined protocol/boundary; it is not a
universal property of a distributed business action. Use idempotency and
durable state even when a broker advertises transactional features.

### How does Kafka ordering work?

**Answer:** A topic has partitions; a producer key selects a partition, and
records are ordered by offset within that partition. A consumer group assigns
each partition to one consumer at a time. Ordering is not global across a
topic. Increasing partitions can change key-to-partition relationships and
consumer assignment.

```text
topic orders
  partition 0: order-a offset 10 -> 11 -> 12
  partition 1: order-b offset 20 -> 21
consumer group: one member owns each partition at a time
```

An offset commit records progress; it does not roll back a database write.
Commit after the side effect is safe and make replay idempotent.

### What causes a Kafka rebalance?

**Answer:** Consumer membership or partition assignment changes, such as a
consumer joining/leaving, a failure, or topic partition change. Rebalances
pause/reassign ownership and can cause duplicate processing around an
uncommitted offset. Keep processing bounded, use cooperative strategies where
appropriate, and monitor lag/rebalance frequency.

### RabbitMQ versus Kafka versus SQS?

**Answer:** RabbitMQ is a broker with exchanges, bindings, queues,
acknowledgements, and routing patterns. Kafka is a durable partitioned log with
replay and independent consumer groups. SQS is a managed queue with visibility
timeout and delete-after-processing semantics. State whether you need replay,
per-key ordering, work distribution, routing, retention, or managed operations.

### What is the outbox pattern?

**Answer:** Write the business row and an unsent event in one local database
transaction. A publisher reads the outbox and sends the event; it marks the
outbox after successful publication. Duplicates remain possible, so consumers
need an inbox/idempotency key. CDC can read the database log and publish
changes, but a database change is not automatically a business event.

```text
business update + outbox insert --one DB commit-->
outbox poller/CDC -> broker -> idempotent consumer
```

### 2PC versus Saga?

**Answer:** Two-phase commit coordinates prepare/commit across resource
managers but holds locks, depends on all participants, and is operationally
fragile at scale. A Saga commits local transactions and uses events or an
orchestrator plus compensating actions. Compensation is a new business action,
not a time machine: it can fail, be delayed, or require manual repair.

### Choreography versus orchestration?

**Answer:** Choreography lets services react to events without a central
coordinator, which reduces central coupling but can hide the workflow.
Orchestration makes state/transitions explicit in a coordinator such as a
workflow engine, but creates an important dependency. Choose based on workflow
complexity, auditability, and ownership.

### CAP and PACELC?

**Answer:** Under a network partition, a distributed system must trade
consistency and availability for a given operation. PACELC adds that when there
is no partition, systems still trade latency against consistency. State the
operation and failure model; CAP does not mean “pick any two database letters”
for every request.

### What do CQRS and event sourcing add?

**Answer:** CQRS separates command/write models from query/read models when
their scaling or shape differs. Event sourcing stores an append-only history of
domain events and rebuilds state from them. Both add replay/versioning,
projection lag, schema evolution, storage, and operational complexity. Use them
for an explicit audit/replay/query-shape need, not as default architecture.

## Configuration and code walkthrough

For a real Kafka consumer, a production configuration must state:

```properties
bootstrap.servers=broker:9092
group.id=orders-projector-v1
enable.auto.commit=false
auto.offset.reset=earliest
max.poll.interval.ms=300000
```

These are illustrative settings, not a safe universal production profile.
Choose serializer/schema compatibility, security protocol, retry/DLT policy,
poll limits, and offset strategy together. RabbitMQ needs durable queue/exchange
configuration, manual acknowledgements, prefetch, and dead-letter routing. SQS
needs a visibility timeout longer than normal work plus a DLQ/redrive policy.

## Failure exercise

Modify `MessageSemantics.java` so the consumer fails after applying the business
effect but before recording the event ID. Run it twice and observe the
duplicate. Then move the idempotency record earlier and explain the opposite
failure: the record can exist even when the side effect did not complete.

## Revision card

```text
At-least-once means duplicates are normal.
Ordering is scoped to a partition/key/queue, not automatically global.
Ack/offset commit is not a database commit.
Outbox makes local write + event intent atomic, not the whole world.
Consumers need durable idempotency and schema evolution.
Sagas compensate business actions; they do not undo history magically.
```
