# Interview question deck

This is the formatted prompt bank from the original question list. Read a
question aloud, answer it without notes, and then open the linked topic guide
for the explanation, code, configuration, flow, and failure exercise.

Use the [revision index](README.md) to study in order. The questions are
prompts, not a substitute for running the linked code.

## Core Java and JVM

Answers and runnable references: [01-core-java-jvm.md](01-core-java-jvm.md)

### OOP, Memory & JVM Internals

- What are the core differences between an abstract class and an interface in modern Java (including default, static, and private methods)?
- Why does Java not support multiple inheritance through classes, and how does it resolve the diamond problem with default interface methods?
- Explain the JVM memory areas: Heap (Young Generation: Eden, Survivor S0/S1; Old Generation), Metaspace, Stack, PC Register, and Native Method Stack?
- How does garbage collection work, and what are the operational differences between Serial, Parallel, G1GC, and ZGC?
- What causes OutOfMemoryError versus StackOverflowError, and how do you diagnose a memory leak in production using heap dumps?
- Explain strong, weak, soft, and phantom references, and where WeakHashMap is practically useful?
- How does the String Constant Pool work, and what does String.intern() actually do?
- Why is String immutable in Java, and what are the exact rules to make a custom class truly immutable?
- What is the contract between equals() and hashCode(), and what breaks in HashMap or HashSet if you violate it?
- Differentiate between final, finally, and finalize(), and explain why finalize() was deprecated in favor of Cleaner?
- What is the difference between Comparable and Comparator, and when would you implement both?
- Compare checked exceptions, unchecked exceptions, and Error, and explain how try-with-resources works under the hood (AutoCloseable)?
- How does Java pass arguments to methods (pass-by-value vs. pass-by-reference for object references)?
- Explain ClassLoaders in Java (Bootstrap, Platform/Extension, Application) and the Delegation Hierarchy principle?
- What is Reflection, how does it break encapsulation, and what performance or security trade-offs does it carry?

### Collections & Generics

- Walk through the exact internal implementation of HashMap (put, get, hash computation, bucket collision, load factor, resizing, and treeification at threshold 8)?
- How do LinkedHashMap and TreeMap work internally, and how do you build an LRU Cache using LinkedHashMap?
- Compare ArrayList vs. LinkedList vs. CopyOnWriteArrayList in terms of memory layout, time complexity, and iteration behavior?
- What is the difference between fail-fast and fail-safe iterators (ConcurrentModificationException)?
- How does ConcurrentHashMap achieve thread safety in Java 8+ (CAS operations + synchronized bucket heads) compared to Java 7 segment locking?
- Why does ConcurrentHashMap forbid null keys and null values while HashMap allows one null key?
- Compare HashSet, LinkedHashSet, and TreeSet, and explain how HashSet uses a HashMap internally?
- Explain Generics type erasure, bounded wildcards (? extends T vs? super T — PECS principle), and why you cannot create generic arrays?

### Multithreading, Concurrency & Modern Java (Java 8–21+)

- Differentiate between thread lifecycle states (NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED)?
- Compare synchronized blocks/methods with ReentrantLock, ReadWriteLock, and StampedLock?
- What does the volatile keyword guarantee, what does it NOT guarantee, and how does it relate to CPU caches and the Java Memory Model (happens-before)?
- How do AtomicInteger and AtomicReference work without locks (Compare-And-Swap / CAS and the ABA problem)?
- Explain ThreadLocal, its internal ThreadLocalMap, and why failing to call remove() causes memory leaks in thread pools?
- Compare ExecutorService thread pool types (FixedThreadPool, CachedThreadPool, SingleThreadExecutor, ScheduledThreadPool) and ThreadPoolExecutor core parameters (corePoolSize, maxPoolSize, keepAliveTime, workQueue, RejectedExecutionHandler)?
- What is a deadlock, livelock, and starvation, and how do you prevent or detect a deadlock in a running JVM?
- Compare Runnable vs. Callable vs. Future vs. CompletableFuture (thenApply, thenCompose, allOf, exceptionally)?
- What are Virtual Threads (Project Loom), how do they map onto carrier platform threads, and what is thread pinning?
- Explain StructuredTaskScope (Structured Concurrency) and ScopedValue as modern replacements for ExecutorService and ThreadLocal?
- Explain functional interfaces (Predicate, Function, Consumer, Supplier), method references, and lambda variable capture rules (effectively final)?
- Compare intermediate vs. terminal operations in Java Streams, lazy evaluation, map() vs. flatMap(), and when parallelStream() degrades performance?
- How do Optional methods (orElse vs. orElseGet vs. orElseThrow) differ in execution behavior?
- How do Java Records, Sealed Classes, and Pattern Matching (instanceof and switch expressions) improve domain modeling?

## Spring Boot and persistence

Answers and runnable references: [04-spring-boot-web-security.md](04-spring-boot-web-security.md)

### Core IoC, Beans & Auto-Configuration

- What is Inversion of Control (IoC) and Dependency Injection (DI), and how do BeanFactory and ApplicationContext differ?
- Compare constructor injection, setter injection, and field injection—why is constructor injection strongly preferred?
- Trace the complete lifecycle of a Spring Bean from instantiation to destruction (BeanNameAware, BeanFactoryAware, BeanPostProcessor, @PostConstruct, InitializingBean, @PreDestroy)?
- Explain all Spring Bean scopes (singleton, prototype, request, session, application, websocket) and how to inject a prototype bean into a singleton bean (@Lookup, ObjectFactory, scoped proxy)?
- How does @SpringBootApplication work internally (@SpringBootConfiguration, @EnableAutoConfiguration, @ComponentScan)?
- Explain the exact mechanism behind Spring Boot Auto-Configuration (META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports and @ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty)?
- How do you resolve circular dependencies in Spring, and why does Spring Boot disable them by default?
- Differentiate between @Component, @Service, @Repository, and @Controller, and explain what extra behavior @Repository provides (exception translation)?
- Compare @Bean vs. @Component, and explain proxyBeanMethods = true vs. false on @Configuration?
- How does Spring Boot externalize configuration, and what is the exact precedence order among command-line args, environment variables, application-{profile}.yml, and application.yml?
- Compare @Value vs. @ConfigurationProperties (type safety, validation, relaxed binding)?

### Spring MVC, REST & AOP

- Trace the full request lifecycle through DispatcherServlet (HandlerMapping, HandlerAdapter, HttpMessageConverter, Controller, ExceptionResolver)?
- Differentiate between @Controller and @RestController, and between @RequestParam, @PathVariable, and @RequestBody?
- Compare Spring Filter (Servlet level) vs. HandlerInterceptor (Spring MVC level) vs. AOP Aspect in execution order and use cases?
- How do you implement global exception handling in Spring Boot using @RestControllerAdvice and @ExceptionHandler, and how does RFC 7807 ProblemDetail work?
- Explain Spring AOP terminology (Aspect, JoinPoint, Pointcut, Advice: @Before, @After, @Around) and compare JDK Dynamic Proxies vs. CGLIB proxies?
- Why does self-invocation inside the same bean bypass @Transactional, @Cacheable, and @Async proxies?
- How do you secure a Spring Boot REST API with Spring Security (SecurityFilterChain, JWT stateless authentication, password encoding, CORS, CSRF)?
- How do Spring Boot Actuators work, how do you secure actuator endpoints, and how do you create custom health indicators and Micrometer metrics?

### Spring Data JPA, Transactions & Performance

- Compare JPA, Hibernate, and Spring Data JPA, and explain the Hibernate Persistence Context and entity states (Transient, Persistent, Detached, Removed)?
- Compare Hibernate First-Level Cache (Session) vs. Second-Level Cache vs. Query Cache?
- What is the N+1 select problem in JPA/Hibernate, and how do you solve it (JOIN FETCH, @EntityGraph, @BatchSize, DTO projections)?
- Compare FetchType.LAZY vs. FetchType.EAGER, and explain what causes LazyInitializationException?
- How does @Transactional work internally, and what are the 7 propagation levels (REQUIRED, REQUIRES_NEW, SUPPORTS, MANDATORY, NOT_SUPPORTED, NEVER, NESTED)?
- Explain transaction isolation levels (READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE) and the anomalies they prevent (dirty reads, non-repeatable reads, phantom reads)?
- Why does @Transactional roll back on unchecked exceptions by default but not on checked exceptions, and how do you customize rollbackFor?
- Compare Optimistic Locking (@Version) vs. Pessimistic Locking (@Lock(LockModeType.PESSIMISTIC_WRITE)) in JPA?
- How do connection pools like HikariCP work, and which pool parameters matter most under high concurrency?

## Microservices and distributed systems

Answers and runnable references: [06-microservices-communication.md](06-microservices-communication.md)

### Architecture, Decomposition & Communication

- Compare Monolithic, Modular Monolith, and Microservices architectures—when should you NOT choose microservices?
- How do you decompose a monolith into microservices using Domain-Driven Design (Bounded Contexts, Aggregates, Ubiquitous Language) and the Strangler Fig pattern?
- Compare synchronous communication (REST, gRPC, GraphQL) vs. asynchronous event-driven communication (Kafka, RabbitMQ)—when do you use each?
- How does an API Gateway (Spring Cloud Gateway, Kong) work, and what responsibilities belong in the gateway versus individual services (routing, rate limiting, auth offloading, protocol translation)?
- Compare the Backend for Frontend (BFF) pattern with a single unified API Gateway?
- How does Service Discovery work (Client-Side vs. Server-Side discovery, Eureka, Consul, Kubernetes DNS/Services)?
- Compare Client-Side Load Balancing (Spring Cloud LoadBalancer) vs. Server-Side Load Balancing (NGINX, AWS ALB, Envoy)?
- How do you handle service-to-service authentication and zero-trust security (mTLS, OAuth2 Client Credentials, JWT propagation)?

### Distributed Data, Consistency & Messaging

- Why is the Database-per-Service pattern essential in microservices, and how do you perform cross-service queries (API Composition vs. CQRS)?
- Explain the CAP Theorem and PACELC Theorem with concrete database examples?
- Compare Two-Phase Commit (2PC / XA transactions) with the Saga Pattern—why does 2PC fail to scale in microservices?
- Compare Choreography-based Sagas vs. Orchestration-based Sagas (e.g., Temporal, Camunda), including failure compensation handling?
- Explain the Transactional Outbox Pattern and Change Data Capture (CDC with Debezium) to guarantee atomic database writes and message publishing?
- How do you guarantee idempotency in REST APIs and Kafka/RabbitMQ consumers when duplicate messages occur (Idempotency Keys, deduplication tables)?
- Explain Event Sourcing and CQRS (Command Query Responsibility Segregation)—what problems do they solve and what operational complexity do they add?
- In Apache Kafka, explain partitions, consumer groups, offset commits, rebalancing, dead-letter topics (DLT), and ordering guarantees?

### Resilience, Observability & Deployment

- Explain the Circuit Breaker pattern (Closed, Open, Half-Open states) and how Resilience4j implements Circuit Breaker, Retry, Rate Limiter, Time Limiter, and Bulkhead?
- Why can naive retries cause cascading retry storms, and how do exponential backoff and jitter prevent them?
- Explain the Three Pillars of Observability (Logs, Metrics, Distributed Tracing) and how traceId and spanId propagate across services via OpenTelemetry / Micrometer Tracing?
- How do you manage centralized configuration across environments (Spring Cloud Config, Vault, Kubernetes ConfigMaps/Secrets) with zero-downtime secret rotation?
- Compare Blue-Green Deployment, Canary Release, and Rolling Updates for backward-compatible microservice releases?
- How do you handle API versioning and schema evolution (Avro/Protobuf backward/forward compatibility, Expand-and-Contract database migrations)?
- What is a Service Mesh (Istio, Linkerd), and how does the Sidecar pattern offload retries, mTLS, and telemetry from application code?

## Design patterns and cloud architecture

Answers and runnable references: [13-design-patterns.md](13-design-patterns.md)

### SOLID & Core Principles

- Explain all five SOLID principles with concrete Java/Spring refactoring examples where violating each principle breaks maintainability?
- Compare Composition over Inheritance, DRY, KISS, YAGNI, and the Law of Demeter?

### Creational Patterns

- Singleton: Write a thread-safe Singleton using Double-Checked Locking (volatile), Bill Pugh Static Inner Class, and Enum—explain how Reflection, Serialization, and Cloning break classic Singletons.Factory Method vs. Abstract Factory: When do you use Factory Method vs. Abstract Factory, and how do you eliminate if-else / switch chains using a Spring Map<String, Strategy> bean registry?
- Builder: Why is the Builder pattern preferred over telescoping constructors, and how does it enforce immutability and object validation at build time?
- Prototype: Compare shallow copy vs. deep copy in the Prototype pattern and why Cloneable is generally avoided in Java?

### Structural Patterns

- Adapter vs. Facade vs. Proxy: Differentiate their intent—give real Spring/JDK examples of each (InputStreamReader, JdbcTemplate, Spring AOP/Hibernate lazy proxies).Decorator: How does the Decorator pattern add responsibilities dynamically without subclassing, and how is it implemented across java.io streams?
- Composite & Bridge: When do you use Composite for tree hierarchies and Bridge to decouple abstraction from implementation?

### Behavioral Patterns

- Strategy: Implement a clean Strategy pattern in Spring Boot using an interface and dependency injection of a List or Map of beans.Observer / Event-Driven: How does the Observer pattern work, and how does Spring's ApplicationEventPublisher + @EventListener / @TransactionalEventListener implement it?
- Template Method: Explain the Hollywood Principle ("Don't call us, we'll call you") and how JdbcTemplate / RestTemplate use Template Method with callbacks.Chain of Responsibility: How do Servlet Filters, Spring Security Filter Chains, and validation pipelines implement Chain of Responsibility?
- Command, State, and Visitor: Contrast the State pattern with the Strategy pattern, and explain when Command (undo/queue) or Visitor applies?

### Cloud & Enterprise Architectural Patterns

- Sidecar & Ambassador Patterns: How do they isolate cross-cutting infrastructure concerns in containerized deployments?
- Anti-Corruption Layer (ACL): How do you protect a clean microservice domain model from a messy legacy system's schema?
- Cache-Aside, Read-Through, Write-Through, and Write-Behind: Compare caching strategies and how to prevent Cache Stampede (Thundering Herd) and Cache Penetration?
