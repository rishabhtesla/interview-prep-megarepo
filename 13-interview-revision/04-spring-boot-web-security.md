# Spring Boot, MVC, AOP, and REST security

## Mental model and request flow

Spring is an object graph and a set of boundary adapters. The container creates
beans, injects dependencies, and applies framework proxies. MVC turns an HTTP
request into a controller call and turns the result into an HTTP response.

```text
client
  -> servlet/security filters
  -> DispatcherServlet
  -> HandlerMapping
  -> HandlerAdapter
  -> argument conversion + Bean Validation
  -> controller
  -> service/use case
  -> repository or remote client
  -> response DTO
  -> HttpMessageConverter
  -> exception resolver / ProblemDetail
  -> client
```

## Run the repository example

From `04-backend-spring-microservices`:

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl zero-to-hero-service spring-boot:run
curl -i -X POST http://127.0.0.1:8083/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"item":"Java course","quantity":2,"unitPriceCents":1500}'
```

Read `OrderController` -> `OrderService` -> `OrderStore` in that order. The
beginner service deliberately uses an in-memory store so the HTTP and bean
boundaries are visible before JPA is introduced.

## Interview questions and answers

### What do IoC and dependency injection mean?

**Answer:** Inversion of Control means application code does not construct and
own every dependency; the container manages object creation and lifecycle.
Dependency injection supplies required collaborators, preferably through a
constructor. `ApplicationContext` extends the lower-level bean factory with
events, resources, message resolution, and automatic post-processing.

### Why is constructor injection preferred?

**Answer:** Required dependencies are explicit, the object can be immutable,
tests can construct it without reflection, and a circular dependency is exposed
instead of hidden. Setter injection is suitable for optional reconfiguration;
field injection hides dependencies and makes construction invalid outside Spring.

### What does `@SpringBootApplication` combine?

**Answer:** It combines `@SpringBootConfiguration`,
`@EnableAutoConfiguration`, and `@ComponentScan`. Auto-configuration imports
conditional configuration when classes/properties are present and a user bean
does not already provide the role.

```text
class path + properties
  -> AutoConfiguration.imports
  -> @ConditionalOnClass / @ConditionalOnProperty
  -> @ConditionalOnMissingBean
  -> bean definitions
  -> post-processors/proxies
```

### `@Component`, `@Service`, `@Repository`, `@Controller`, and `@Bean`?

**Answer:** They all participate in component registration, but the stereotype
communicates intent. `@Repository` also enables persistence exception
translation. `@Service` marks an application/domain service and `@Controller`
marks an MVC adapter. `@Bean` registers the object returned by a configuration
method, which is useful for third-party classes or explicit construction.
`proxyBeanMethods=true` preserves inter-method `@Bean` calls through a
configuration proxy; `false` avoids that proxy when methods are independent.

### How does configuration precedence work?

**Answer:** Spring Boot combines packaged defaults, profile-specific files,
environment/config imports, system properties, and command-line arguments; the
more specific/external source overrides the earlier default. The exact list
depends on Boot version and config import mechanisms, so verify the version
when debugging. `@ConfigurationProperties` binds a typed, validated group;
`@Value` is convenient for a small scalar but scatters strings and validation.

```java
@ConfigurationProperties("catalog")
@Validated
record CatalogProperties(@NotBlank String baseUrl, Duration readTimeout) {}
```

### How do you resolve a circular dependency?

**Answer:** Prefer redesigning ownership so the dependency graph is acyclic.
Extract a shared policy, introduce an event/port, or invert one dependency.
Setter/lazy injection can break construction cycles but may create partially
initialized objects; enabling circular references globally hides a design smell.

### What is the bean lifecycle?

**Answer:** Spring instantiates the bean, injects dependencies, invokes aware
callbacks, runs `BeanPostProcessor` before initialization, invokes
`@PostConstruct`/`InitializingBean`, exposes the initialized bean, and later
invokes destruction callbacks such as `@PreDestroy`. Avoid business work in
constructors or initialization that depends on a transaction or remote service.

### How do you handle a prototype inside a singleton?

**Answer:** A singleton is created once, so directly injecting a prototype
creates only one prototype instance. Request a fresh instance through
`ObjectProvider<T>`, `ObjectFactory<T>`, `@Lookup`, or a scoped proxy. Choose
the mechanism based on lifecycle and testability.

### Why does self-invocation bypass `@Transactional`, `@Cacheable`, and `@Async`?

**Answer:** Spring commonly applies those behaviors through a proxy. A call
from one method to another method on `this` never crosses the proxy, so the
interceptor does not run. Move the operation to another bean, call through a
designed proxy boundary, or use a programmatic mechanism when appropriate.

### What is the MVC difference between controller annotations?

**Answer:** `@Controller` participates in MVC and commonly returns a view;
`@RestController` is `@Controller` plus `@ResponseBody`, so return values are
serialized. `@PathVariable` identifies a resource in the URI,
`@RequestParam` supplies a query/form parameter, and `@RequestBody` deserializes
the request payload.

### Filter, interceptor, or AOP?

**Answer:** A servlet filter runs before Spring MVC and is good for raw HTTP
concerns such as correlation IDs. A `HandlerInterceptor` knows the MVC handler
and is useful for request authorization or timing. AOP wraps method execution
inside the application and is useful for transactions, caching, or metrics.
Choose the narrowest boundary that owns the concern.

### How do global exception handling and `ProblemDetail` work?

**Answer:** `@RestControllerAdvice` centralizes exception-to-status/body
mapping, while `@ExceptionHandler` maps a known exception. RFC 7807
`ProblemDetail` gives clients a stable type/title/status/detail/instance shape.
Do not expose stack traces, SQL, tokens, or internal class names in `detail`.
Keep validation errors, missing resources, conflicts, and dependency failures
distinguishable.

### JDK dynamic proxy versus CGLIB proxy?

**Answer:** A JDK proxy implements interfaces and intercepts interface calls;
CGLIB creates a subclass and can proxy concrete classes/methods that are
overridable. Final classes/methods and self-invocation limit subclass proxying.
The proxy is an implementation mechanism, not permission to assume every call
is transactional.

### How should a REST API secure itself?

**Answer:** Authenticate at a trusted boundary, authorize each resource/action,
hash passwords with a slow adaptive encoder, validate input, use TLS, define
CORS intentionally, and make JWT access-token verification stateless when that
is the chosen model. CSRF protection is required for browser cookie
authentication; a bearer token in an `Authorization` header has a different
threat model. Never treat a client-supplied user ID as identity.

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
  return http
      .csrf(csrf -> csrf.disable()) // only for a stateless bearer-token API
      .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
      .authorizeHttpRequests(a -> a
          .requestMatchers("/actuator/health").permitAll()
          .anyRequest().authenticated())
      .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
      .build();
}
```

This is an interview configuration sketch, not a replacement for threat
modeling. Do not disable CSRF in a cookie-authenticated browser application.

### What are Actuator and Micrometer for?

**Answer:** Actuator exposes operational endpoints such as health and metrics.
Expose only the endpoints needed, secure sensitive details, and make health
represent dependency readiness deliberately. Micrometer records counters,
gauges, timers, and distribution data for a backend. A metric without a bounded
tag cardinality can become a memory/monitoring incident.

## Configuration walkthrough

The local progress service uses:

```properties
server.address=127.0.0.1
server.port=8082
spring.jpa.open-in-view=false
spring.mvc.problemdetails.enabled=true
catalog.base-url=${CATALOG_BASE_URL:http://127.0.0.1:8081}
catalog.connect-timeout=1s
catalog.read-timeout=2s
management.endpoints.web.exposure.include=health,info
```

Configuration precedence is designed so command-line arguments and environment
variables can override packaged defaults. Keep secrets outside source control;
use a secrets manager or deployment secret reference.

## Failure exercise

Send an invalid order:

```bash
curl -i -X POST http://127.0.0.1:8083/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"item":"","quantity":0,"unitPriceCents":-1}'
```

Expect a `400` Problem Details response and no stored order. Then make the
service calculation overflow and decide whether the API should return a client
error, a domain error, or an internal error. Do not return `200` with an empty
or partial success object.

## Revision card

```text
Constructor injection exposes required dependencies.
Auto-configuration is conditional and backs off for user beans.
MVC maps HTTP to a controller; services own use cases.
Validation at the boundary does not replace business invariants.
Spring AOP needs a proxy boundary; self-invocation bypasses it.
Security authenticates and authorizes; it does not make data ownership automatic.
Actuator/metrics are operational contracts and must be secured and bounded.
```
