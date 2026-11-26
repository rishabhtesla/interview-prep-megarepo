# Resume technology coverage

This index prevents the repository from implying that one Spring or React
example covers an entire resume skill. Every row must eventually link to a
topic guide, a runnable example, and a test or observable check.

## Current tracks

| Resume area | Current location | Learning entry point |
|---|---|---|
| Java 8/11/17, OOP, collections, exceptions | `01-core-java` | [`MASTERCLASS.md`](01-core-java/MASTERCLASS.md) · [`LEARNING_GUIDE.md`](01-core-java/LEARNING_GUIDE.md) |
| Java concurrency, multithreading, JMM, JVM | `01-core-java` | `ThreadsAndVisibility`, `ExecutorsAndFutures`, `JvmAndValues` |
| Scala language and functional programming | `02-scala-basics` | [`MASTERCLASS.md`](02-scala-basics/MASTERCLASS.md) · [`LEARNING_GUIDE.md`](02-scala-basics/LEARNING_GUIDE.md) |
| Scala 2.12 and Spark-compatible code | `03-spark-distributed-computing/scala` | [`LEARNING_GUIDE.md`](03-spark-distributed-computing/LEARNING_GUIDE.md) |
| Spark Core, SQL, partitioning, skew, streaming concepts | `03-spark-distributed-computing` | [`MASTERCLASS.md`](03-spark-distributed-computing/MASTERCLASS.md) · [`LEARNING_GUIDE.md`](03-spark-distributed-computing/LEARNING_GUIDE.md) |
| Spring Boot, MVC, REST, JPA, Hibernate | `04-backend-spring-microservices` | [`MASTERCLASS.md`](04-backend-spring-microservices/MASTERCLASS.md) · [`LEARNING_GUIDE.md`](04-backend-spring-microservices/LEARNING_GUIDE.md) |
| Inter-service HTTP communication | `04-backend-spring-microservices` | [`INTERSERVICE_COMMUNICATION.md`](04-backend-spring-microservices/INTERSERVICE_COMMUNICATION.md) |
| React, JavaScript, Vite, testing, async UI, GeoJSON mutation visualization | `06-frontend-react-ui` | [`LEARNING_GUIDE.md`](06-frontend-react-ui/LEARNING_GUIDE.md) |
| DSA and LeetCode patterns | `05-dsa-leetcode` | [`LEARNING_GUIDE.md`](05-dsa-leetcode/LEARNING_GUIDE.md) |
| Python 3.10/3.12, async code, FastAPI | `07-python-fastapi` | [`LEARNING_GUIDE.md`](07-python-fastapi/LEARNING_GUIDE.md) |
| JavaScript ES6+, TypeScript, Express | `08-javascript-typescript-express` | [`LEARNING_GUIDE.md`](08-javascript-typescript-express/LEARNING_GUIDE.md) |
| Message contracts, delivery semantics, retries, DLQs | `09-messaging-streaming` | [`LEARNING_GUIDE.md`](09-messaging-streaming/LEARNING_GUIDE.md) |
| SQL, Redis TTL, Docker, Kubernetes, IaC, CI/CD | `10-storage-cloud-devops` | [`LEARNING_GUIDE.md`](10-storage-cloud-devops/LEARNING_GUIDE.md) |
| Semantic search, reranking, agents, MCP, knowledge graphs | `11-genai-search-agents` | [`LEARNING_GUIDE.md`](11-genai-search-agents/LEARNING_GUIDE.md) |
| Apache Camel, SOAP, Spring Cloud Eureka concepts | `12-enterprise-integration` | [`LEARNING_GUIDE.md`](12-enterprise-integration/LEARNING_GUIDE.md) |
| Interview questions across the complete resume | `13-interview-revision` | [`README.md`](13-interview-revision/README.md) |

## Topics requiring dedicated reference modules

These are explicitly mentioned in the supplied resume and must not be hidden
inside a generic project guide:

| Area | Topics to cover |
|---|---|
| JavaScript/TypeScript | Runtime schema validation, API integration testing, production Express deployment |
| Python | Packaging, FastAPI integration tests, persistence, production deployment |
| Spring ecosystem | Spring Cloud Eureka client/server integration with a real registry |
| Messaging | Real Kafka, RabbitMQ/AMQP, AWS SQS clients, retries, DLQs, ordering, idempotency |
| Streaming | Flink/Flink SQL execution, Kafka Streams, state, watermarks, checkpoints, CDC |
| Akka | Actors, supervision, persistence, cluster sharding, duplicate-event handling |
| Frontend UI | React 18 compatibility, TypeScript UI build, Tailwind/shadcn setup, MapLibre GL tile integration |
| Data stores | PostgreSQL/MySQL integration, DynamoDB, S3, Parquet, JSONL/GZIP, pgvector |
| Cloud | EC2, EMR/Graviton, Lambda, Step Functions, Bedrock, credentialed examples |
| Delivery | Docker, Kubernetes, CDK, CloudFormation, GitLab CI/CD, Maven, SBT |
| Quality/security | SonarQube, JaCoCo, JUnit 5, Mockito, dependency/CVE handling |
| GenAI | Model-backed agent workflows, official MCP SDK, evaluation, AI coding agents |

## Definition of complete

A row is complete only when its module contains:

```text
beginner lesson -> intermediate lesson -> advanced lesson
learning guide with execution flow and code actions
small runnable example
failure example
automated test or deterministic check
interview questions and revision card
```

Cloud examples must provide a local mode or clearly document the required AWS
account, region, permissions, cost, and cleanup. No credentials belong in this
repository.
