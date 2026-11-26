# Messaging and streaming: from one message to reliable event processing

Messaging systems are easy to demo and easy to misunderstand. This track starts
with a standard-library Java simulation so delivery, retry, duplicate, and
dead-letter behavior are visible without installing Kafka or RabbitMQ. After
that, compare the vocabulary and code shape with real brokers.

## Setup

```bash
cd 09-messaging-streaming
./run.sh
```

The local example uses Java 17 and no external dependency.

## Lesson 1 — A message is a contract

Source: [`src/MessageSemantics.java`](src/MessageSemantics.java).

A message contains:

```text
event ID
event type
aggregate/entity ID
schema version
payload
occurred-at timestamp
```

The consumer must validate the contract before applying a side effect. A
message is not automatically a command, and an event is not automatically a
database transaction.

Run:

```bash
./run.sh
```

The example publishes an `OrderCompleted` event, delivers it twice, and shows
that the idempotent consumer applies the business effect once.

## Lesson 2 — Delivery semantics

| Semantics | Meaning | What the consumer must do |
|---|---|---|
| At-most-once | May lose a message; no duplicate after acknowledgement | Use only when loss is acceptable |
| At-least-once | Message is retried until acknowledged; duplicates are possible | Make processing idempotent |
| Exactly-once | A bounded guarantee inside a defined system/protocol | State the boundary; never promise it casually |

Most practical broker consumers are at-least-once. Acknowledging before the
database write risks loss. Acknowledging after the write permits a crash window
that can cause a duplicate. The handler needs an idempotency key, unique
constraint, or inbox record.

## Lesson 3 — Retry and dead-letter behavior

The local example retries a transient failure with a bounded attempt count. After
the limit, it moves the message to a dead-letter collection instead of silently
discarding it.

Production retry questions:

- Is the error transient or permanent?
- Is the operation safe to retry?
- Is backoff exponential with jitter?
- Is the retry count visible?
- Does a poison message block unrelated work?
- Who replays the dead-letter message and how is it corrected?

Never retry a malformed payload forever. Never treat a dead-letter queue as a
permanent database without retention and replay policy.

## Lesson 4 — Broker semantics without infrastructure

Source: [`src/BrokerSemantics.java`](src/BrokerSemantics.java).

Run:

```bash
./run.sh
```

The second local program makes four production boundaries visible:

- Kafka-like partitions keep records with the same key together, so ordering
  is scoped to a partition.
- RabbitMQ-like exchange routing sends a message to queues by routing key.
- SQS-like visibility hides an unacknowledged message temporarily, after which
  it can be delivered again.
- Flink-like event-time windows close when a watermark passes the window end.
- Actor-like state is updated by the owner for one incident key.

These are intentionally small simulations. They do not provide broker
durability, network behavior, serialization compatibility, or real checkpoint
recovery. Their job is to make the vocabulary testable before introducing
Docker or cloud credentials.

## Lesson 5 — Kafka

Kafka vocabulary:

```text
topic -> partitions -> offsets
consumer group -> one consumer owns a partition at a time
producer key -> determines partition choice and ordering scope
```

Use Kafka when durable ordered logs, replay, and multiple independent consumer
groups matter. Ordering is normally guaranteed **within a partition**, not
across a whole topic.

Typical producer action:

```java
producer.send(new ProducerRecord<>("orders", orderId, eventJson));
```

The key chooses the partitioning relationship. A consumer reads records and
commits an offset after its side effect is safely handled. Offset commit is not
the same as database commit.

Interview follow-ups:

- What if a consumer crashes after writing the DB and before committing?
- How do you preserve order for one order ID?
- How do you replay a topic without repeating a charge?
- What happens when partitions increase?

## Lesson 6 — RabbitMQ and AMQP

RabbitMQ separates:

```text
producer -> exchange -> binding/routing key -> queue -> consumer
```

Use it when work queues, explicit acknowledgements, routing patterns, and
dead-letter exchanges are useful. A consumer acknowledges after its side effect
is complete. A rejected/requeued message can be delivered again.

Typical action:

```java
channel.basicPublish("orders", "completed", messageBytes);
channel.basicConsume("billing", false, consumer);
```

The exchange routes; the queue stores; the consumer performs work. Do not call
an exchange a queue or assume publishing means a consumer has processed it.

## Lesson 7 — AWS SQS

SQS is a managed queue with visibility timeout:

```text
receive -> message hidden temporarily -> process -> delete
                                  \-> timeout -> visible again
```

If processing takes longer than visibility timeout, another consumer can see
the message. Extend visibility for long work and make processing idempotent.
FIFO queues add ordering/deduplication features with throughput trade-offs.

## Lesson 8 — Flink and Flink SQL

Flink is a stateful stream processor. Distinguish:

```text
event time     = when the event happened
processing time = when the job observed it
watermark      = progress estimate for event-time completeness
state          = information retained across events
checkpoint     = recoverable operator state
```

Example Flink SQL shape:

```sql
SELECT customer_id, window_start, COUNT(*) AS order_count
FROM TABLE(
  TUMBLE(TABLE orders, DESCRIPTOR(event_time), INTERVAL '5' MINUTES)
)
GROUP BY customer_id, window_start, window_end;
```

The window emits based on event time and watermark progress, not simply on the
arrival order. Define a late-event policy before claiming the result is final.

## Lesson 9 — Akka actors and stateful deduplication

An actor owns state and processes messages sequentially within its mailbox:

```text
incident ID -> actor/shard -> merge event -> persist state -> publish update
```

This can make per-key state changes easier to reason about, but it does not
remove network failure, persistence failure, duplicate delivery, or cluster
rebalance concerns. Cluster sharding assigns entities to nodes; it does not
make arbitrary side effects atomic.

For duplicate incident events, define:

- Event identity and deduplication window.
- Merge predicate.
- State persistence.
- Recovery behavior.
- What happens when the same event arrives after failover.

## Lesson 10 — Change Data Capture and outbox/inbox

Source: [`src/OutboxInbox.java`](src/OutboxInbox.java).

CDC turns database changes into events:

```text
database commit log -> CDC connector -> topic -> consumers/projections
```

The ordering and transaction boundary depend on the connector and database.
Consumers still need schema evolution, replay, idempotency, and deletion
semantics. A CDC event is not automatically a business event; map it carefully.

The outbox example writes a business row and an unsent event in one local
transaction, then publishes the event separately. The inbox records an event ID
before applying its side effect, so a duplicate delivery is ignored. This is
not magic exactly-once processing: the database transaction, broker publish,
consumer transaction, and side effect must still have an explicit failure
policy.

## Failure exercise

Modify `MessageSemantics.java` so the consumer fails after applying the business
effect but before marking the event as processed. Run it twice and observe the
duplicate. Then move the idempotency record before the effect and explain the
opposite failure: a crash can suppress work that never completed.

The correct production design depends on the side effect. A database unique
constraint, transactional inbox/outbox, or idempotency key may be appropriate.

## Interview answer

> “I assume at-least-once delivery unless the system gives a narrower guarantee.
> I acknowledge only after the side effect is safe, but that leaves a duplicate
> window, so the handler is idempotent using an event ID and durable
> deduplication. I classify errors into retryable and permanent, use bounded
> backoff, route poison messages to a dead-letter path, and monitor lag,
> retries, age, and processing failures. Ordering is scoped to a partition,
> queue, or key—not automatically global.”
