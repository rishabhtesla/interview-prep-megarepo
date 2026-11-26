# Scala, Python, JavaScript, TypeScript, and React

## Why these belong together

The resume spans JVM languages and browser/server JavaScript. The common
interview question is not syntax; it is evaluation, mutability, type/runtime
boundaries, asynchronous work, and failure handling.

## Run each foundation

```bash
# Scala 3
cd 02-scala-basics
scala-cli run src/main/scala --server=false \
  --main-class interviewprep.scalabasics.ZeroToHero

# Python/FastAPI
cd 07-python-fastapi
python3 -m unittest discover -s tests -v
python3 examples/01_basics.py

# JavaScript/TypeScript/Express
cd 08-javascript-typescript-express
npm test
npm run typecheck

# React
cd 06-frontend-react-ui
npm test
npm run build
```

## Scala interview questions

### Scala 3 versus Spark Scala 2.12?

**Answer:** The basics track uses Scala 3.3.6 and Scala CLI. Spark 3.5.6 in
this repository uses Scala 2.12.18 binary artifacts. Scala source syntax and
library artifacts must match the runtime; do not copy Scala 3 `given` syntax
into the Scala 2.12 Spark build.

### `Option`, `Either`, and `Future`?

**Answer:** `Option` models presence/absence, `Either` models a value or typed
failure, and `Future` represents eventual completion on an execution context.
They make the boundary explicit but do not remove the need to handle empty
values, failures, executor ownership, and timeouts.

Run `02-scala-basics/src/main/scala/ZeroToHero.scala`, then inspect
`AlgebraicDataTypes.scala` and `FuturesAndEffects.scala`.

## Python and FastAPI questions

### What does `async` change?

**Answer:** `async def` creates a coroutine; it does not run it until awaited
by an event loop. `asyncio.gather` can overlap independent non-blocking I/O.
Blocking `time.sleep`, CPU-heavy work, or a synchronous database driver still
blocks the event-loop thread.

### Where should FastAPI validation live?

**Answer:** Pydantic validates request shape/types at the HTTP boundary.
Business invariants belong in a service/domain layer, and durable invariants
belong in the database as well. A response model prevents accidental leakage
of internal fields.

```text
HTTP -> Pydantic model -> endpoint -> service -> repository -> response model
```

Run `07-python-fastapi/examples/02_async_work.py` and
`07-python-fastapi/app/main.py`; compare the boundary with Spring DTO
validation.

## JavaScript and TypeScript questions

### What is the event loop?

**Answer:** JavaScript runs synchronous code on a call stack and schedules
promise continuations/timers through queues serviced by the event loop. `await`
pauses the async function, not the whole runtime, unless the code before/after
it blocks. CPU-bound work needs decomposition, workers, or another process.

### Why is TypeScript not runtime validation?

**Answer:** TypeScript types are erased during compilation. JSON from a network
is still untrusted at runtime, so validate it with a schema or explicit
narrowing before using it. `unknown` forces narrowing; `any` suppresses useful
checking.

### `Promise.all` versus `Promise.allSettled`?

**Answer:** `Promise.all` succeeds only when all required operations succeed and
rejects on the first failure. `allSettled` reports every outcome and is useful
when optional widgets can fail independently. Use sequential `await` when the
second operation depends on the first.

Run `08-javascript-typescript-express/src/01-basics.mjs` and
`src/app.mjs`; inspect the app factory separately from `src/server.mjs`.

## React questions and render flow

```text
user event
  -> handler
  -> state update
  -> render with a state snapshot
  -> DOM commit
  -> effect/fetch synchronization
  -> success/error/empty UI
```

### Why does React rerender?

**Answer:** A state or parent-prop update schedules a render. React calls the
component to calculate the next UI, reconciles it, and commits necessary DOM
changes. State is a snapshot for that render; calling a setter does not mutate
the current render's variable.

### When should you use an effect?

**Answer:** Use an effect to synchronize React with an external system such as
network, subscription, timer, or DOM API. Do not use it to calculate a value
that can be derived during render. Include dependencies and cleanup to prevent
stale requests or subscriptions.

### Controlled versus uncontrolled input?

**Answer:** A controlled input gets its value from React state and reports
changes through an event; this makes validation and conditional UI explicit.
An uncontrolled input keeps its value in the DOM and is read through a ref.
Choose based on ownership and complexity, but always provide labels and an
accessible error/status path.

### How do you test React behavior?

**Answer:** Render the component, interact as a user would through accessible
roles/labels, and assert visible behavior. Test loading, success, empty,
failure, retry, cancellation/stale responses, and keyboard use. Avoid tests
that only assert internal state variables.

Run `06-frontend-react-ui/src/App.test.jsx`, inspect `useStudy.js`,
`TopicCard.jsx`, and `AlgorithmLab.jsx`. The full React course is
`06-frontend-react-ui/COURSE.md`.

### How would you explain a GeoJSON/MapLibre UI?

**Answer:** The backend owns a validated GeoJSON contract and mutation stages;
the React layer owns selection, loading/error state, and presentation. Keep
features stable with IDs, validate geometry/type/properties at the boundary,
diff stage outputs rather than mutating the source object, and avoid rebuilding
the whole map for an unrelated UI state. MapLibre is a rendering adapter, not
the source of truth for domain mutations.

## Configuration map

| Runtime | Configuration to inspect |
|---|---|
| Scala 3 | `02-scala-basics/src/main/scala/project.scala` |
| Spark Scala 2.12 | `03-spark-distributed-computing/scala/build.sbt` |
| Python | `07-python-fastapi/requirements.txt` |
| Express | `08-javascript-typescript-express/package.json` |
| TypeScript | `08-javascript-typescript-express/tsconfig.json` |
| React/Vite | `06-frontend-react-ui/package.json`, `vite.config.*` |
| GeoJSON mutation UI | `06-frontend-react-ui/src/GeoJsonMutationLab.jsx` |

## Revision card

```text
Scala makes data/error/effect types explicit; binary versions still matter.
Python async overlaps non-blocking I/O but does not fix blocking code.
TypeScript checks author code, not untrusted runtime JSON.
Promise composition must reflect dependency and failure semantics.
React state is a render snapshot; effects synchronize external systems.
UI tests should assert user-visible behavior and accessible failure states.
```
