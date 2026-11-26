# Interview Prep Megarepo

A hands-on course for Java/backend, full-stack, and data engineering interviews.
The goal is not to memorize definitions: it is to **explain a concept, implement
it, demonstrate its limits, and defend a trade-off under questioning**.

No repository can guarantee an interview result. This course gives you a structured
way to expose gaps and practice. Completing a page is not the same as mastering it.

## Start here

1. Take the diagnostic in [STUDY_PLAN.md](STUDY_PLAN.md) without looking up answers.
2. Read [how to study one topic](TOPIC_LESSON_FORMAT.md), then follow the progressive [full-stack learning path](LEARNING_PATH.md) from basics to advanced; shorten it only after passing the exit criteria.
3. Read a topic, predict the example's behavior, run it, change an assumption, and explain the result aloud.
4. Solve DSA problems before opening their solutions. Re-solve missed problems after 1, 3, 7, and 21 days.
5. Use [INTERVIEW_PLAYBOOK.md](INTERVIEW_PLAYBOOK.md) for timed practice and scoring.
6. Use the [topic-wise interview revision directory](13-interview-revision/README.md)
   when you need questions, answers, flow diagrams, configuration, and runnable
   code references in one place.

Use [RESUME_COVERAGE.md](RESUME_COVERAGE.md) as the technology checklist. It
separates topics that already have runnable lessons from resume technologies
that still need their own focused reference module.

## A runnable path from basics to full stack

Use [LEARNING_PATH.md](LEARNING_PATH.md) as the route through the repository.
It connects Java fundamentals, DSA, SQL/Spark, Spring services, React, and
system design into one progression. The React dashboard is the interactive
front door: it saves study progress, exposes loading/error/retry behavior, and
includes a step-by-step binary-search visualizer. The backend failure lab then
lets you observe what happens when a dependency is stopped.

## Learning tracks

| Track | What to study | What mastery looks like |
| --- | --- | --- |
| [01-core-java](01-core-java/LEARNING_GUIDE.md) | Language fundamentals, JVM, OOP, collections, concurrency, functional APIs | Explain correctness, allocation, complexity, and thread-safety choices with runnable examples |
| [02-scala-basics](02-scala-basics/LEARNING_GUIDE.md) | Expressions, ADTs, collections, error handling, traits, type classes, futures | Model invalid states explicitly and compose transformations without accidental side effects |
| [03-spark-distributed-computing](03-spark-distributed-computing/LEARNING_GUIDE.md) | Scala/PySpark batch jobs, joins, windows, partitioning, query plans, data quality | Explain where data moves, why a stage is slow, and how to make reruns safe |
| [04-backend-spring-microservices](04-backend-spring-microservices/LEARNING_GUIDE.md) | REST services, persistence, validation, interservice calls, failure semantics | Trace a request across service boundaries and distinguish local transactions from distributed guarantees |
| [05-dsa-leetcode](05-dsa-leetcode/LEARNING_GUIDE.md) | Categorized Java solutions with correctness reasoning and complexity | Derive a pattern from constraints, state its invariant, and handle boundary cases under time pressure |
| [06-frontend-react-ui](06-frontend-react-ui/LEARNING_GUIDE.md) | React application connected to the backend, hooks, state, asynchronous UI, testing, and an interactive algorithm lab | Explain rerenders, ownership of state, race conditions, accessibility, and failure states |
| [07-python-fastapi](07-python-fastapi/LEARNING_GUIDE.md) | Python 3.10+, typing, async work, FastAPI validation, and API boundaries | Explain coroutine scheduling, validation, dependency boundaries, and failure handling |
| [08-javascript-typescript-express](08-javascript-typescript-express/LEARNING_GUIDE.md) | Modern JavaScript, TypeScript, Promises, Express, and runtime validation | Explain event-loop behavior, type erasure, middleware order, and API errors |
| [09-messaging-streaming](09-messaging-streaming/LEARNING_GUIDE.md) | Delivery semantics, retries, DLQs, Kafka, RabbitMQ, SQS, Flink, Akka, and CDC concepts | State the delivery boundary, ordering scope, retry policy, and idempotency strategy |
| [10-storage-cloud-devops](10-storage-cloud-devops/LEARNING_GUIDE.md) | SQL, indexes, Redis TTL, object storage, Docker, Kubernetes, IaC, and CI/CD | Choose a storage layer, explain a deployment artifact, and diagnose a failed pipeline |
| [11-genai-search-agents](11-genai-search-agents/LEARNING_GUIDE.md) | Semantic retrieval, reranking, graphs, tool-using agents, and MCP boundaries | Explain evidence, authorization, tool safety, budgets, and unsupported answers |
| [12-enterprise-integration](12-enterprise-integration/LEARNING_GUIDE.md) | Apache Camel EIP routes, SOAP XML boundaries, and Eureka-style service discovery | Trace a message route, harden an XML boundary, and separate discovery from health |

Each track's **Learning guide** is its single entry point and contains its setup
commands. It links to deeper course, masterclass, source, and exercise files
where they exist.
There is deliberately **no one-command root build**: Java, Scala CLI, sbt,
Python/Spark, Maven, and Node have different runtimes. Run commands from the
directory specified in the relevant learning guide.

## Cross-track interview preparation

| Guide | Purpose |
| --- | --- |
| [STUDY_PLAN.md](STUDY_PLAN.md) | Diagnostic, weekly learning objectives, retrieval practice, role-specific priorities, exit criteria |
| [INTERVIEW_PLAYBOOK.md](INTERVIEW_PLAYBOOK.md) | Timed coding/debugging/design interviews, answer frameworks, scored rubrics, worked probes |
| [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) | Capacity estimation and worked designs for checkout, analytics, URL shortening, and study progress |
| [SQL_AND_DATA_MODELING.md](SQL_AND_DATA_MODELING.md) | Joins, windows, indexes, transactions, pagination, query exercises with worked answers |
| [BEHAVIORAL_AND_PROJECT_STORIES.md](BEHAVIORAL_AND_PROJECT_STORIES.md) | Honest project narratives, ownership, conflict, incidents, and evidence-based communication |
| [13-interview-revision](13-interview-revision/README.md) | Topic-wise questions, answers, flows, configuration maps, failure drills, and code references for the complete resume |

## Toolchain and version boundaries

The Java learning examples target Java 17. Follow each Scala and Spark track's
pinned version instructions: Scala 3 learning syntax must not be pasted into
a Scala 2 Spark job, and a Spark artifact's Scala binary version must match
its runtime. Use a supported Python version for the selected Spark release.
Frontend dependencies and the Node requirement are documented in that track.

Use isolated environments where practical: a Python virtual environment for
PySpark and project-local npm dependencies. Do not change a global JDK or Python
installation simply to make one example run.

## How to use solutions without fooling yourself

For each exercise, record your first attempt, not just the final passing code.
If you read a hint, mark the result "assisted." On a later attempt, close the
solution and derive it from a blank file. Recognition is easier than recall.

| Evidence | Not enough | Ready to move on |
| --- | --- | --- |
| Concepts | "I have read about visibility" | Explain why a visible counter increment can still lose updates |
| Coding | Copying a correct implementation | State the invariant, implement from scratch, and test an adversarial input |
| Backend | Endpoint returns HTTP 200 | Explain invalid input, retries, conflicts, dependency failure, and persistence |
| Spark | Job runs on tiny local data | Predict shuffles and explain skew, executor failure, and idempotent output |
| React | Happy-path screen renders | Handle loading, empty, error, rapid interaction, and keyboard use |
| Design | Naming Kafka, Redis, and Kubernetes | Justify which are needed, what fails, and what consistency is promised |

## Scope and safety

The runnable application is a **local learning system**, not a production platform.
Read its documented limitations before exposing it to a network. Authentication,
authorization, production secrets, deployment hardening, and large-scale operational
guarantees need deliberate designs; discussing them in a guide does not implement them.
Use synthetic data, never production credentials or personal data, in exercises.

The parent e-commerce workspace is independent of this course. All course files live
inside `interview-prep-megarepo/`.
