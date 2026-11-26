# Interview revision: questions, answers, flows, code, and configuration

This directory is the interview-facing index for the technologies represented
in the resume. It is designed for someone returning to hands-on coding after a
gap: each topic starts with the mental model, names the request/data flow,
points to the exact runnable code and configuration, and then gives interview
questions with concise answers and follow-up traps.

The source code remains in the learning tracks. This directory is deliberately
an explanation and revision layer, not a second copy of every implementation.
When a guide says **Run**, execute the linked example before reading the answer.

## How to use one topic

1. Read the flow diagram and write down what each boundary owns.
2. Read the question without the answer and speak for two minutes.
3. Run the linked example and inspect the configuration file it uses.
4. Change one input or failure condition and predict the result.
5. Re-read the answer, then explain the trade-off and one production risk.
6. Write a five-line revision card from memory the next day.

Use the sequence **concept -> flow -> code -> configuration -> failure -> answer**.
Do not claim an external product is implemented when the repository contains
only a local simulation; the guides call out those boundaries explicitly.

## Topic route

| Order | Topic guide | Resume areas |
|---:|---|---|
| 1 | [Core Java, JVM, and OOP](01-core-java-jvm.md) | Java 8/11/17, OOP, memory, GC, class loading, exceptions, reflection |
| 2 | [DSA and LeetCode patterns](15-dsa-leetcode.md) | Constraints, invariants, arrays, graphs, backtracking, dynamic programming |
| 3 | [Collections and generics](02-collections-generics.md) | HashMap, collections, generics, PECS, LRU, fail-fast behavior |
| 4 | [Concurrency and modern Java](03-concurrency-modern-java.md) | JMM, volatile, CAS, executors, CompletableFuture, virtual threads, streams |
| 5 | [Spring Boot, Web, and Security](04-spring-boot-web-security.md) | IoC, auto-configuration, MVC, AOP, REST, Security, Actuator |
| 6 | [JPA, transactions, and performance](05-jpa-transactions-performance.md) | Hibernate, persistence context, locking, isolation, N+1, HikariCP |
| 7 | [Microservices and communication](06-microservices-communication.md) | DDD, discovery, gateways, REST/gRPC, BFF, service authentication |
| 8 | [Messaging and distributed consistency](07-messaging-distributed-consistency.md) | Kafka, RabbitMQ, SQS, CDC, outbox/inbox, Saga, CQRS, idempotency |
| 9 | [Resilience, observability, and delivery](08-resilience-observability-delivery.md) | Resilience4j, retries, tracing, metrics, config, deployment, service mesh |
| 10 | [Scala, Python, JavaScript, and React](09-scala-python-javascript-react.md) | Scala 2/3, FastAPI, Express, TypeScript, React, UI state and testing |
| 11 | [Spark, Flink, and Akka](10-spark-flink-akka.md) | Spark SQL, partitioning, skew, Flink state/time, Akka actors/sharding |
| 12 | [Storage, cloud, containers, and CI/CD](11-storage-cloud-devops.md) | PostgreSQL, MySQL, Redis, DynamoDB, S3, Parquet, Docker, Kubernetes, AWS |
| 13 | [Enterprise integration and GenAI](12-enterprise-integration-genai.md) | Camel, SOAP, Eureka, semantic search, agents, MCP, knowledge graphs |
| 14 | [Design patterns](13-design-patterns.md) | SOLID, GoF patterns, Spring pattern mapping, cloud architecture patterns |
| 15 | [System design and behavioral answers](14-system-design-behavioral.md) | Capacity, consistency, incidents, ownership, project storytelling |

The original question bank is preserved in [QUESTIONS.md](QUESTIONS.md), grouped
by the same areas. Use it as a timed prompt deck after completing a guide.

## Code and configuration map

| Area | Runnable entry point | Important configuration |
|---|---|---|
| Core Java | `01-core-java/src/main/java/com/interviewprep/core/ZeroToHero.java` | `--release 17`, `01-core-java/LEARNING_GUIDE.md` |
| DSA | `05-dsa-leetcode/run-examples.sh` | Java 17, deterministic `main` examples |
| Spring services | `04-backend-spring-microservices/zero-to-hero-service` | `pom.xml`, `src/main/resources/application.properties` |
| Distributed Spring | `04-backend-spring-microservices/catalog-service` and `progress-service` | `application-local.properties`, HTTP client timeouts |
| React | `06-frontend-react-ui/src/App.jsx` | `package.json`, Vite proxy, Vitest |
| Python/FastAPI | `07-python-fastapi/app/main.py` | `requirements.txt`, Pydantic request models |
| TypeScript/Express | `08-javascript-typescript-express/src/app.mjs` | `package.json`, `tsconfig.json` |
| Spark | `03-spark-distributed-computing/pyspark/zero_to_hero.py` | `requirements.txt`, Spark/Scala binary matrix |
| Messaging | `09-messaging-streaming/src/MessageSemantics.java` | Java 17, no broker required for the local simulation |
| Storage/deployment | `10-storage-cloud-devops/docker/Dockerfile` | Kubernetes manifest, GitLab pipeline, SQL fixtures |
| Enterprise integration | `12-enterprise-integration/src/main/java/com/interviewprep/integration/CamelRouteLesson.java` | `pom.xml`, Camel dependency version |
| GenAI | `11-genai-search-agents/examples/semantic_search.py` | Standard library local vector simulation |

## Answer format

Use this structure in an interview:

```text
Definition: what the mechanism guarantees.
Flow: where the request/data moves.
Code: one implementation detail or API.
Failure: what happens when the dependency or assumption breaks.
Trade-off: when I would choose another design.
Measurement: which log, metric, trace, or test proves it.
```

The strongest answer is not the longest answer. It states the boundary of the
guarantee and does not confuse a framework feature with a distributed guarantee.
