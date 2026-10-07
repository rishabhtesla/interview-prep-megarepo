# Interview Prep Megarepo

A hands-on course for Java/backend, full-stack, and data engineering interviews.
The goal is not to memorize definitions: it is to **explain a concept, implement
it, demonstrate its limits, and defend a trade-off under questioning**.

No repository can guarantee an interview result. This course gives you a structured
way to expose gaps and practice. Completing a page is not the same as mastering it.

## Start here

1. Take the diagnostic in [STUDY_PLAN.md](STUDY_PLAN.md) without looking up answers.
2. Pick a role emphasis and follow the 12-week progression; shorten it only after passing the exit criteria.
3. Read a topic, predict the example's behavior, run it, change an assumption, and explain the result aloud.
4. Solve DSA problems before opening their solutions. Re-solve missed problems after 1, 3, 7, and 21 days.
5. Use [INTERVIEW_PLAYBOOK.md](INTERVIEW_PLAYBOOK.md) for timed practice and scoring.

## Six learning tracks

| Track | What to study | What mastery looks like |
| --- | --- | --- |
| [01-core-java](01-core-java/README.md) | Language fundamentals, JVM, OOP, collections, concurrency, functional APIs | Explain correctness, allocation, complexity, and thread-safety choices with runnable examples |
| [02-scala-basics](02-scala-basics/README.md) | Expressions, ADTs, collections, error handling, traits, type classes, futures | Model invalid states explicitly and compose transformations without accidental side effects |
| [03-spark-distributed-computing](03-spark-distributed-computing/README.md) | Scala/PySpark batch jobs, joins, windows, partitioning, query plans, data quality | Explain where data moves, why a stage is slow, and how to make reruns safe |
| [04-backend-spring-microservices](04-backend-spring-microservices/README.md) | REST services, persistence, validation, interservice calls, failure semantics | Trace a request across service boundaries and distinguish local transactions from distributed guarantees |
| [05-dsa-leetcode](05-dsa-leetcode/README.md) | 50 categorized Java solutions with correctness reasoning and complexity | Derive a pattern from constraints, state its invariant, and handle boundary cases under time pressure |
| [06-frontend-react-ui](06-frontend-react-ui/README.md) | React application connected to the backend, hooks, state, asynchronous UI, testing | Explain rerenders, ownership of state, race conditions, accessibility, and failure states |

Each track's README is its entry point and contains its own setup commands.
There is deliberately **no one-command root build**: Java, Scala CLI, sbt,
Python/Spark, Maven, and Node have different runtimes. Run commands from the
directory specified in the relevant README.

## Cross-track interview preparation

| Guide | Purpose |
| --- | --- |
| [STUDY_PLAN.md](STUDY_PLAN.md) | Diagnostic, weekly learning objectives, retrieval practice, role-specific priorities, exit criteria |
| [INTERVIEW_PLAYBOOK.md](INTERVIEW_PLAYBOOK.md) | Timed coding/debugging/design interviews, answer frameworks, scored rubrics, worked probes |
| [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) | Capacity estimation and worked designs for checkout, analytics, URL shortening, and study progress |
| [SQL_AND_DATA_MODELING.md](SQL_AND_DATA_MODELING.md) | Joins, windows, indexes, transactions, pagination, query exercises with worked answers |
| [BEHAVIORAL_AND_PROJECT_STORIES.md](BEHAVIORAL_AND_PROJECT_STORIES.md) | Honest project narratives, ownership, conflict, incidents, and evidence-based communication |

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
