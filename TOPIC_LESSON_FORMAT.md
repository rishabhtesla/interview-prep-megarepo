# How to study one topic in this repository

This repository is an interview reference, not a collection of classes to
memorize. A topic is complete only when you can explain what happens, run the
smallest example, change one assumption, and discuss the failure mode.

## The six-pass loop

Use this loop for **every** lesson, whether it is Java, Kafka, Spring, React,
Spark, SQL, or an algorithm.

### Pass 1: orient yourself

Read only:

1. The learning objective.
2. The vocabulary table.
3. The diagram or request/data flow.
4. The "what to observe" section.

Before reading the implementation, answer:

```text
What problem is this solving?
Who owns the state?
What enters the system?
What leaves the system?
What can fail?
```

### Pass 2: run the smallest example

Use the command shown in the lesson. Do not start with the largest application.
Record:

- The command you ran.
- The input.
- The output.
- Any ports, files, queues, database rows, or logs created.

If a lesson needs an external service, it must document a local mode or clearly
label the cloud-only step. Never put credentials into the repository.

### Pass 3: read the code in execution order

Read code in the order the runtime uses it:

```text
entry point
  -> input parsing / HTTP route / consumer
  -> validation
  -> main transformation or business action
  -> storage / downstream call / output
  -> error handling
  -> test
```

For each important line, say what action it performs. For example:

```java
catalog.requireTopic(topicId);
```

means: make a synchronous HTTP request to another service, turn a 404 into a
not-found response, turn a timeout into a 503, and do not write progress if the
remote validation fails.

### Pass 4: change one assumption

Use the lesson's change exercise. Examples:

- Empty input or duplicate values for DSA.
- A timeout or 404 for service communication.
- A null field for an API request.
- A hot key for Spark.
- A cache miss or expired TTL for Redis.
- A failed form submission for React.

Predict the result before running it.

### Pass 5: prove it with a test

Find or add the smallest test that demonstrates the behavior. A useful test
names the behavior, not an implementation detail:

```text
when the catalog is unavailable, progress is not written
```

Tests should cover a success path, an empty/boundary path, and a failure path.

### Pass 6: answer aloud

Use this interview structure:

```text
Definition
-> execution flow
-> small example
-> trade-off
-> failure mode
-> test and observability
```

If you cannot explain the trade-off or failure mode, repeat the lesson.

## Required sections for each topic guide

Each topic guide in this repository should answer these questions in order:

1. **Goal:** what will I know after this lesson?
2. **Prerequisites:** what should I already understand?
3. **Vocabulary:** what do the important words mean?
4. **Mental model:** what is the simplest correct picture?
5. **Flow:** how does input move through the system?
6. **Smallest example:** what is the minimum runnable code?
7. **Code actions:** what does each important line do?
8. **Run it:** exact commands and expected output.
9. **Change it:** one guided modification.
10. **Failure behavior:** what happens when something goes wrong?
11. **Testing:** which behavior proves the lesson?
12. **Trade-offs:** when is this approach a poor choice?
13. **Interview questions:** what follow-ups are likely?
14. **Revision card:** five-minute summary.

Short code snippets belong in the guide for fast revision. Complete examples
belong in source files so they can be compiled and tested. The guide must link
to the exact source and test file; a code block without an execution path is
not considered a complete example.

## How to schedule revision

For a new lesson, use 45–90 minutes:

| Time | Activity |
|---:|---|
| 5 min | Recall prerequisites without opening the solution |
| 10 min | Read the mental model and predict the flow |
| 15 min | Run and inspect the smallest example |
| 20 min | Change one assumption and test it |
| 10 min | Explain the answer aloud |
| 5 min | Write one mistake and one follow-up question |

Revisit difficult lessons after 1, 3, 7, and 21 days. A revision pass should
start from the revision card and one code example, not from rereading every
paragraph.
