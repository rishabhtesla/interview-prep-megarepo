# JPA, Hibernate, transactions, locking, and performance

## Mental model and flow

JPA is a specification; Hibernate is an implementation; Spring Data JPA provides
repository abstractions and query integration. The persistence context is a
first-level identity map and change tracker around a transaction.

```text
HTTP/use case
  -> @Transactional proxy
  -> persistence context
  -> entity state transition
  -> dirty checking / SQL flush
  -> database transaction commit
  -> detached objects after context ends
```

## Run the repository example

```bash
cd 04-backend-spring-microservices
mvn -pl catalog-service,progress-service test
```

Read `StudyProgress`, `ProgressRepository`, `ProgressStore`, and
`ProgressIntegrationTest`. The local services use file-backed H2 and
`spring.jpa.open-in-view=false`, making transaction boundaries visible.

## Entity states

| State | Meaning |
|---|---|
| Transient | New object not associated with a persistence context |
| Managed/persistent | Context tracks it and can dirty-check changes |
| Detached | Previously managed but context is closed or entity is cleared |
| Removed | Marked for deletion at flush/commit |

## Interview questions and answers

### First-level cache versus second-level/query cache?

**Answer:** The first-level cache belongs to one persistence context and
guarantees one managed object identity per database identity in that context.
A second-level cache is shared across sessions and needs invalidation/consistency
policy. A query cache stores query result identities/metadata and can become
stale or expensive to invalidate. Do not enable caches without measuring hit
rate, invalidation, memory, and correctness.

### What is the N+1 query problem?

**Answer:** One query loads parent rows and then a separate query runs for each
parent's lazy association. Fix the specific use case with a fetch join,
`@EntityGraph`, batch fetching, or a DTO projection; do not make every
association eager. Inspect generated SQL and row multiplicity.

```java
@Query("""
       select distinct o
       from Order o
       join fetch o.items
       where o.id = :id
       """)
Optional<Order> findWithItems(long id);
```

### Lazy versus eager loading?

**Answer:** Lazy loading defers a relationship until access; eager loading
loads it earlier. Lazy is a safer default for large graphs, but access outside
an active context causes `LazyInitializationException`. Eager can silently
load huge graphs and create joins/N+1 behavior elsewhere. Design fetch plans
per use case and keep the persistence boundary explicit.

### How does `@Transactional` work?

**Answer:** A Spring proxy intercepts the call, obtains/binds a transaction,
invokes the target, flushes/commits on success, and rolls back according to the
exception policy. A call must cross the proxy. The default propagation is
`REQUIRED`; other modes define whether work joins, creates, suspends, requires,
or forbids a transaction.

| Propagation | Meaning |
|---|---|
| `REQUIRED` | Join current or create one |
| `REQUIRES_NEW` | Suspend current and create independent one |
| `SUPPORTS` | Join if present, otherwise run without one |
| `MANDATORY` | Fail if none exists |
| `NOT_SUPPORTED` | Suspend current and run without one |
| `NEVER` | Fail if one exists |
| `NESTED` | Savepoint when the transaction manager supports it |

### What do isolation levels prevent?

**Answer:** `READ_UNCOMMITTED` permits dirty reads. `READ_COMMITTED` prevents
dirty reads but can see a changed row twice. `REPEATABLE_READ` usually prevents
non-repeatable reads and database-specific phantom behavior differs.
`SERIALIZABLE` gives the strongest isolation at the cost of concurrency.
Isolation is a database/driver behavior; state the actual database before
making a guarantee.

### Why do unchecked exceptions roll back by default?

**Answer:** Spring treats unchecked exceptions and `Error` as indications that
the operation failed unexpectedly; checked exceptions may represent a handled
business condition. Customize intentionally with `rollbackFor`, and never
catch an exception inside a transaction merely to return a success response
without deciding whether the transaction should commit.

### Optimistic versus pessimistic locking?

**Answer:** Optimistic locking stores a version and fails a conflicting update
when the version changed; it is good for short transactions and low conflicts.
Pessimistic locking asks the database to lock rows while work proceeds; it can
prevent conflicts but reduces concurrency and can deadlock/time out. A retry
after optimistic conflict must re-read and re-evaluate business rules.

```java
@Version
private long version;
```

### What matters in HikariCP?

**Answer:** Pool size, connection timeout, idle/max lifetime, leak detection,
database capacity, and query duration matter. A larger pool is not automatically
faster: it can overload the database and increase queueing. Set a bounded
timeout and measure active/idle/pending connections and query latency.

## Configuration walkthrough

The local catalog configuration is intentionally explicit:

```properties
spring.datasource.url=jdbc:h2:file:./data/catalog;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
```

`ddl-auto=update` is a teaching convenience. Production uses reviewed,
versioned migrations such as Flyway/Liquibase, secrets outside source, backups,
and a tested rollback/expand-contract plan.

## Failure exercise

Start two concurrent updates to the same `StudyProgress` row. Observe the
optimistic-lock conflict. Then add a fetch join for a collection and inspect
whether pagination or duplicate parent rows changed. The proof is the SQL log
and database state, not just a green controller test.

## Revision card

```text
JPA is a spec; Hibernate implements it; Spring Data supplies repositories.
Persistence context tracks identity and dirty changes within a unit of work.
Lazy/eager is a fetch-plan decision, not a universal performance switch.
Transactions are local boundaries unless a distributed protocol exists.
Optimistic locking detects conflicts; pessimistic locking blocks conflicts.
Pool size must match database capacity and measured concurrency.
```
