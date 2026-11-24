# Python and FastAPI: beginner to interview-ready

This track teaches Python 3.10+ and FastAPI in small steps. It is not a
collection of unrelated scripts. Read one lesson, run its example, change one
input, and answer the checkpoint before moving on.

## What this track covers

```text
Python syntax
-> data structures and functions
-> errors, modules, and typing
-> testing
-> synchronous and asynchronous code
-> HTTP and JSON
-> FastAPI routes and validation
-> persistence and dependency boundaries
-> production concerns
```

## Prerequisites and setup

Python 3.10+ is enough for the first lessons:

```bash
cd 07-python-fastapi
python3 --version
python3 -m unittest discover -s tests -v
python3 examples/01_basics.py
python3 examples/02_async_work.py
```

The FastAPI lessons use a virtual environment:

```bash
python3 -m venv .venv
. .venv/bin/activate
python -m pip install -r requirements.txt
uvicorn app.main:app --reload
```

Open `http://127.0.0.1:8000/docs` to inspect the generated OpenAPI UI.

## Lesson 1 — Python values, collections, and functions

Source: [`examples/01_basics.py`](examples/01_basics.py)

Learn:

- Names, values, mutability, and references.
- Lists, tuples, dictionaries, and sets.
- Comprehensions.
- Functions, default arguments, and type annotations.
- Why a mutable default argument is dangerous.

Run:

```bash
python3 examples/01_basics.py
```

What the code does:

1. Builds a list of orders.
2. Filters completed orders.
3. Groups totals by customer.
4. Prints a deterministic result.
5. Demonstrates a safe default list created per call.

Change exercise: add an order with a negative amount. Decide whether the
validation belongs in the constructor, a service function, or the API layer.

## Lesson 2 — Errors, modules, and tests

Source: [`examples/01_basics.py`](examples/01_basics.py) and
[`tests/test_basics.py`](tests/test_basics.py).

Python exceptions should communicate an invalid operation or boundary failure.
Catch only errors you can handle. Do not turn a database/network error into an
empty success response.

Run:

```bash
python3 -m unittest discover -s tests -v
```

The tests prove valid totals, rejected amounts, and repeated invocation. Add a
test for an unknown customer before changing the implementation.

## Lesson 3 — Async work

Source: [`examples/02_async_work.py`](examples/02_async_work.py)

`async def` creates a coroutine. It does not execute concurrently until the
coroutine is awaited by an event loop. `asyncio.gather` starts independent
awaitable work together, but it does not make blocking code non-blocking.

```python
results = await asyncio.gather(fetch("catalog"), fetch("pricing"))
```

The example simulates two independent I/O calls and prints elapsed time. Replace
one call with `time.sleep(1)` and observe why blocking work harms an async
server. Use an async library for async I/O or move blocking work to an executor.

## Lesson 4 — FastAPI request flow

Source: [`app/main.py`](app/main.py)

Request flow:

```text
HTTP request
-> route matching
-> Pydantic JSON validation
-> endpoint function
-> domain/service function
-> response model serialization
-> HTTP response
```

The example exposes:

```text
GET  /health
GET  /api/orders
POST /api/orders
```

Run:

```bash
. .venv/bin/activate
uvicorn app.main:app --reload
```

Try a successful request:

```bash
curl -fsS http://127.0.0.1:8000/health
curl -fsS http://127.0.0.1:8000/api/orders
curl -i -X POST http://127.0.0.1:8000/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customer_id":"c1","amount":42.50}'
```

The endpoint validates JSON before the service function runs. The service
creates an order ID and returns a response model; it does not accept arbitrary
fields as trusted data.

Invalid request:

```bash
curl -i -X POST http://127.0.0.1:8000/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customer_id":"","amount":-1}'
```

Expected behavior: FastAPI returns `422` and the in-memory order list is
unchanged. In a production API, confirm the validation error contract with
clients rather than relying on framework wording forever.

## Lesson 5 — Dependency boundaries and persistence

The in-memory repository is intentional for the first lesson. It makes the
request flow visible without hiding it behind an ORM. Replace it only after
you can explain:

```text
route -> validation -> service -> repository -> response
```

Then compare the boundary with Spring Data JPA:

| Python/FastAPI | Spring |
|---|---|
| Pydantic request model | Bean Validation DTO |
| Dependency function | Spring bean injection |
| Repository protocol | Repository interface |
| `HTTPException`/exception handler | `ProblemDetail`/exception handler |
| `pytest`/unittest | JUnit/Mockito |

Do not claim that a framework automatically supplies transactions,
authentication, idempotency, or durable storage.

## Interview questions

1. What is the difference between a list, tuple, set, and dictionary?
2. Why are mutable default arguments dangerous?
3. What is the difference between an exception you handle and one you propagate?
4. What does `async` change, and what does it not change?
5. Why does blocking I/O inside an async endpoint hurt throughput?
6. Where should validation happen: client, API, service, or database?
7. Why use a response model instead of returning an arbitrary dictionary?
8. How would you replace the in-memory repository with PostgreSQL?
9. How would you add authentication without trusting a client-supplied user ID?
10. How would you test a downstream timeout?

## Revision card

```text
Python names refer to objects.
Mutability is different from rebinding.
Exceptions must preserve failure semantics.
async/await coordinates non-blocking I/O; it does not fix blocking work.
FastAPI validates at the boundary, but business invariants need a service/domain layer.
In-memory storage teaches flow, not durability.
```
