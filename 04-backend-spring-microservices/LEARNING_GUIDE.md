# Spring and microservices learning guide

This track is a connected lab, but study each capability in isolation before
trying to run the browser, catalog-service, and progress-service together.
Start with [MASTERCLASS.md](MASTERCLASS.md) and the isolated
`zero-to-hero-service`; it has no database or network dependency.

## Lesson order

| Step | Read first | Run/inspect | What you must explain |
|---:|---|---|---|
| 1 | [`MASTERCLASS.md`](MASTERCLASS.md) | Run `zero-to-hero-service` and inspect the request path | Component scanning, constructor injection, validation, service boundaries, response status |
| 2 | `COURSE.md` section 1 | Start one existing service and inspect bean wiring | Lifecycle, singleton scope, and configuration |
| 3 | `COURSE.md` section 2 and `API.md` | Send valid and invalid HTTP requests | Controller, DTO, validation, status code, response contract |
| 4 | `COURSE.md` section 3 | Trace `ProgressService` to `ProgressStore` | Proxy boundary, transaction scope, self-invocation limitation |
| 5 | `COURSE.md` section 4 | Inspect SQL and entity state | Hibernate persistence context, dirty checking, flush, commit, N+1 |
| 6 | `COURSE.md` section 5 | Run overlapping writes/tests | Optimistic locking, isolation, lost update, retry limits |
| 7 | [Inter-service communication](INTERSERVICE_COMMUNICATION.md) | Stop catalog and send a PUT | Sync HTTP, timeout, 404/503 translation, local versus remote ownership |
| 8 | `COURSE.md` sections 6–9 | Read failure and test paths | Idempotency, remote failure, error contracts, observability |
| 9 | `EXERCISES.md` | Implement one change test-first | Keep behavior explicit and avoid success-shaped fallbacks |
| 10 | `INTERVIEW.md` | Answer aloud without notes | Design trade-offs and operational failure modes |

## The required request trace

For any endpoint, draw this before reading implementation details:

```text
HTTP request
-> routing
-> JSON deserialization
-> validation
-> controller
-> service/use case
-> remote call (if any)
-> transaction
-> repository/database
-> response DTO
-> status/error mapping
```

Then identify which layer performs each action. If a controller performs SQL,
or a repository performs remote HTTP, stop and question the boundary.

## Completion criteria

You are ready to move on when you can:

1. Start the beginner service and each distributed service independently.
2. Call each endpoint with `curl`.
3. Explain every documented 2xx/4xx/5xx path.
4. Stop a dependency and predict the result before testing it.
5. Explain why a local transaction cannot roll back a remote service.
6. Add a test for a new failure without hiding the failure.
