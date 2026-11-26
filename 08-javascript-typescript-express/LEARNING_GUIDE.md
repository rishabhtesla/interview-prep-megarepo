# JavaScript, TypeScript, and Express: beginner to interview-ready

This track supports both React revision and backend JavaScript interviews. Start
with Node's built-in test runner and only then run the Express server.

## Setup

```bash
cd 08-javascript-typescript-express
npm install
npm test
npm run basics
npm run typecheck
```

Start the API:

```bash
npm run server
```

The server listens on `http://127.0.0.1:3000`.

## Lesson 1 — JavaScript values and transformations

Source: [`src/01-basics.mjs`](src/01-basics.mjs)

Learn:

- `const` versus `let`.
- Primitive values versus object references.
- Destructuring and spread.
- `map`, `filter`, and `reduce`.
- Optional chaining and nullish coalescing.
- Why mutation can surprise React components.

Run:

```bash
npm run basics
```

The example filters completed orders and calculates a total. It does not mutate
the input array. Change the input to an empty list and explain the identity
value supplied to `reduce`.

## Lesson 2 — Promises and asynchronous JavaScript

The same source contains `loadDashboard`, which starts independent calls with
`Promise.all`.

```js
const [orders, alerts] = await Promise.all([
  load("/api/orders"),
  load("/api/alerts"),
]);
```

Actions:

1. Start both requests.
2. Wait until both resolve.
3. Reject the combined operation if either request rejects.

For independent optional widgets, compare `Promise.allSettled`. For dependent
operations, use sequential `await` and explain why the dependency exists.

## Lesson 3 — TypeScript contracts

Source: [`src/01-types.ts`](src/01-types.ts).

Run:

```bash
npm run typecheck
```

TypeScript catches a wrong field name and invalid status before the JavaScript
is shipped. Types do not validate untrusted JSON at runtime; validate API
responses at the boundary.

Important interview distinctions:

```text
interface/type -> compile-time shape
unknown -> untrusted value requiring narrowing
any -> disables useful checking
union -> one of several valid states
generic -> reusable relationship between input and output types
```

## Lesson 4 — Express request flow

Source: [`src/app.mjs`](src/app.mjs), started by [`src/server.mjs`](src/server.mjs).

```text
HTTP request
-> middleware
-> route match
-> request validation
-> service action
-> JSON response
-> error middleware
```

Try:

```bash
curl -fsS http://127.0.0.1:3000/health
curl -fsS http://127.0.0.1:3000/api/orders
curl -i -X POST http://127.0.0.1:3000/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"c1","amount":42.50}'
```

Invalid input:

```bash
curl -i -X POST http://127.0.0.1:3000/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"","amount":-1}'
```

Expected behavior: `400`; the service does not append an invalid order.
The error middleware is the final boundary for unexpected failures. It must not
leak stack traces or convert a failed write into a successful response.

## Lesson 5 — Express production concerns

The example is intentionally in-memory. The app factory is separated from the
listener so tests can start an ephemeral HTTP port and exercise the real JSON
boundary. Before calling it production-ready,
add:

- Schema validation such as Zod or a validated TypeScript DTO.
- Authentication and authorization.
- Request size and rate limits.
- Structured logs and correlation IDs.
- Timeouts for downstream calls.
- A database repository and transaction policy.
- Graceful shutdown for HTTP server and database connections.
- Tests for success, validation, dependency failure, and duplicate requests.

## Interview questions

1. What is the event loop?
2. Why does `await` not make CPU-bound work parallel?
3. What is the difference between `Promise.all` and `Promise.allSettled`?
4. Why is `unknown` safer than `any`?
5. What does Express middleware do?
6. Where should request validation happen?
7. Why is client-side TypeScript not a security boundary?
8. How do you handle an error thrown in an async route?
9. How would you shut down an Express service safely?
10. How would you trace one request across multiple services?

## Revision card

```text
Objects are reference values.
Do not mutate data you do not own.
Promise.all fails as a group when one required operation fails.
TypeScript types disappear at runtime.
Validate untrusted JSON at the API boundary.
Middleware is ordered request processing.
Unexpected failures need explicit error handling and observability.
```
