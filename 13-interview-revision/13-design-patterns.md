# Design patterns: principles, code, and Spring mappings

## How to answer a pattern question

Name the **intent**, show the smallest code shape, state the ownership
boundary, and explain when the pattern makes the design worse. A pattern is
not a class-name exercise; it is a trade-off vocabulary.

## SOLID and core principles

| Principle | Interview answer | Smell when it is violated |
|---|---|---|
| Single Responsibility | One reason for change per module/class | Controller validates, charges, persists, and sends email |
| Open/Closed | Extend behavior without editing stable orchestration | Every new payment type adds another conditional |
| Liskov Substitution | A subtype preserves the base contract | Subtype throws for valid base operations |
| Interface Segregation | Clients depend on focused interfaces | One “god interface” forces unused methods |
| Dependency Inversion | Policy depends on abstractions; details are injected | Domain code constructs HTTP/database clients |

Composition over inheritance, KISS, YAGNI, DRY, and the Law of Demeter are
heuristics. DRY the knowledge/invariant, not merely every repeated line; a
premature abstraction can be harder to change than duplication.

## Runnable anchors

```bash
cd 01-core-java
javac --release 17 -Xlint:all -Werror -d out \
  src/main/java/com/interviewprep/core/*.java
java -cp out com.interviewprep.core.ObjectDesign
```

Then inspect the Spring strategy/constructor-injection examples under
`04-backend-spring-microservices/zero-to-hero-service`.

## Creational patterns

### Singleton

**Question:** How do you implement a safe singleton?

**Answer:** Prefer a container-managed singleton or an enum when singleton
identity is genuinely required. If implementing manually, the holder idiom
avoids synchronization on the normal read path. Double-checked locking needs
`volatile` because publication must not expose a partially constructed object.
Reflection, serialization, and cloning can break naive implementations.

```java
public final class Configuration {
  private Configuration() {}

  private static class Holder {
    private static final Configuration INSTANCE = new Configuration();
  }

  public static Configuration instance() {
    return Holder.INSTANCE;
  }
}
```

Do not use a singleton as a disguised global mutable variable.

### Factory and strategy registry

```java
interface PaymentMethod {
  Receipt pay(Money amount);
}

@Service("card")
final class CardPayment implements PaymentMethod { /* ... */ }

@Service("wallet")
final class WalletPayment implements PaymentMethod { /* ... */ }

@Service
final class Checkout {
  private final Map<String, PaymentMethod> methods;

  Checkout(Map<String, PaymentMethod> methods) {
    this.methods = Map.copyOf(methods);
  }
}
```

**Question:** Factory Method versus Abstract Factory?

**Answer:** Factory Method lets a hierarchy choose one product creation
operation. Abstract Factory creates a compatible family of products. In Spring,
a `Map<String, Strategy>` bean registry often removes an `if/else` chain while
keeping creation/dependency wiring visible.

### Builder and prototype

**Answer:** Builder makes optional fields readable, validates invariants before
construction, and supports immutable objects without telescoping constructors.
Prototype copies an existing object; shallow copy shares nested references,
deep copy duplicates owned mutable state. `Cloneable` has awkward protected
semantics and partial-copy surprises; prefer an explicit copy constructor or
factory.

## Structural patterns

| Pattern | Intent | Familiar Java/Spring example |
|---|---|---|
| Adapter | Convert one contract to another | `InputStreamReader`, partner DTO mapper |
| Facade | Simplify a subsystem boundary | Service/use-case class over repositories |
| Proxy | Control/access/intercept a target | Spring AOP, Hibernate lazy proxy |
| Decorator | Add behavior while preserving interface | `BufferedInputStream`, metrics wrapper |
| Composite | Treat tree and leaf uniformly | UI/tree/domain hierarchy |
| Bridge | Vary abstraction and implementation independently | Notification policy + transport |

**Question:** Adapter versus facade versus proxy?

**Answer:** Adapter changes the interface; facade reduces the interface;
proxy preserves the interface while adding access/control/lazy/intercepted
behavior. Calling all three “wrappers” hides the intent.

## Behavioral patterns

### Strategy

Encapsulate an algorithm behind a role and inject the chosen implementation.
Spring's `List<PaymentMethod>` or `Map<String, PaymentMethod>` is a practical
strategy registry. Validate unknown strategy keys and do not let an arbitrary
client string select privileged behavior without authorization.

### Observer and domain events

`ApplicationEventPublisher` plus `@EventListener` decouples reactions, while
`@TransactionalEventListener` can run after a local transaction commits.
Events still need idempotency, ordering, retry, and publication failure policy.
Do not call an in-process event “distributed messaging.”

### Template Method and Chain of Responsibility

Template Method owns an algorithm skeleton and delegates steps to callbacks;
`JdbcTemplate` is a familiar example. Chain of Responsibility passes a request
through ordered handlers; servlet filters, Spring Security filters, and
validation pipelines use this shape. Ordering and short-circuit behavior are
part of the contract.

### State versus Strategy

Strategy chooses an algorithm from outside. State changes the object's behavior
as its internal lifecycle changes. Command represents an action that can be
queued/undone; Visitor adds operations across a stable object structure but
becomes expensive when variants change.

## Cloud/enterprise patterns

| Pattern | Interview explanation |
|---|---|
| Sidecar | Co-located proxy/agent handles telemetry, mTLS, or policy |
| Ambassador | Sidecar-like outbound gateway dedicated to one dependency |
| Anti-Corruption Layer | Translates legacy contracts so the clean domain does not absorb them |
| Cache-aside | Application owns source read/miss/write and invalidation |
| Read-through | Cache layer loads source on a miss |
| Write-through | Cache and source update in one defined cache API operation |
| Write-behind | Cache acknowledges before delayed source write; loss/reordering risk |

Cache policies must define TTL, invalidation, stampede control, penetration
behavior, stale reads, and source-of-truth ownership.

## Failure exercise

Implement a new payment strategy without editing checkout orchestration. Add a
test for an unknown strategy, a failed payment, and repeated invocation. Then
explain which layer owns validation and whether the event is local or
distributed.

## Revision card

```text
Pattern intent matters more than pattern name.
Constructor injection exposes dependencies and supports composition.
Factories remove creation knowledge from callers.
Strategy removes branching when behavior varies by policy.
Proxy preserves a contract while adding control/interception.
Observer/eventing needs ordering, retries, and idempotency.
Cloud patterns move cross-cutting concerns but add operational layers.
```
