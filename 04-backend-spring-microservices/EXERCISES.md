# Backend labs with worked solutions

Work on a copy/branch before changing the course. Each lab specifies an expected
outcome and a solution; extensions below are **not already implemented**.

## Lab 1 — Trace a successful save (30 minutes)

**Task:** Run the system, save `react-effects`, then repeat the same PUT. Draw the
call sequence and identify transaction begin/commit. Verify a subsequent GET and
service restart return the note.

**Acceptance:** one row, stable observable state, data survives a same-directory
restart, no claim that catalog and progress share a transaction.

**Worked solution:**

```text
browser → Vite /api/progress → ProgressController
        → ProgressService → CatalogClient → catalog TopicController/Service/DB
        ← confirmed topic
        → ProgressStore proxy [begin H2 transaction]
        → repository find → replace → saveAndFlush
        ← target returns DTO [proxy commits]
        ← 200 JSON → UI saved count updates
```

GET progress reads only progress DB. A repeated replacement can perform SQL and
increment internal metadata, but it must not create another primary-key record
or flip completion. Files live in the service working directory; changing that
directory changes which file-backed DB you observe.

## Lab 2 — Failure contracts before happy paths (45 minutes)

**Task:** For `{}`, a 501-character note, unknown ID, and stopped catalog, predict
status, body shape, and number of writes. Run each request.

**Acceptance:** 400, 400, 404, 503 respectively; unchanged stored progress.

**Worked solution:** validation rejects request syntax before orchestration.
Catalog existence failure occurs before `ProgressStore`; HTTP/transport failure
becomes 503. `ResponseEntityExceptionHandler` serializes ProblemDetail.
`ProgressServiceTest.catalogFailurePreventsAnyWrite` verifies no store interaction;
the integration test also verifies row count, which catches a write accidentally
performed before throwing.

**Extension:** make the HTTP stub return a 200 response with the wrong topic ID.
Expected 503, since a success status alone does not validate response meaning.

## Lab 3 — Add a validated confidence score (60 minutes)

**Task (extension):** Add a confidence score 1–5 to each progress record, with a
documented default for older requests. Follow request → service/store → entity →
response → integration tests. Don't put JSON validation only in the browser.

**Worked solution sketch:**

```java
public record ProgressRequest(
    @NotNull Boolean completed,
    @Size(max = 500) String note,
    @Min(1) @Max(5) Integer confidence
) {}
// During replacement:
// confidence = request.confidence() == null ? 1 : request.confidence();
```

Add a non-null entity field and response field; define whether omission resets
to 1 (replacement semantics) or preserves existing confidence (PATCH semantics).
For the current PUT choose and document replacement. Tests: missing→1, 1 and 5
accepted, 0/6 rejected, saved GET returns score. Existing file DB rows require a
migration/default strategy; simply relying on `ddl-auto=update` isn't a production
migration plan. Update React only after the API contract is agreed.

## Lab 4 — Reject stale browser edits (90 minutes)

**Task (extension):** Two tabs load the same note. A saves first; B tries to save
old state. Return a precondition failure rather than silently overwrite A.

**Worked design:**

1. Expose a version-derived ETag on a single-progress GET.
2. Require `If-Match` on replacement of existing records.
3. Validate the expected version within the write transaction; retain DB
   `@Version` to catch a race after the check.
4. Return 412 when the precondition fails; return 428 if your contract requires
   the header and it is missing.
5. Define creation separately (`If-None-Match: *` or a documented create command).
6. UI displays both latest server state and retained local draft; no blind retry.

**Tests:** read v1 in A/B; A saves with v1; B saves with v1→412; persisted state
is A's. Also race two requests after both version checks. Checking a version only
in the controller without atomic DB protection is insufficient.

## Lab 5 — Demonstrate N+1 without hiding it (90 minutes)

**Task (extension):** In a scratch learning branch, add Lessons belonging to Topics.
Create a listing that displays lesson counts. Observe SQL for twelve topics.

**Worked solution:** first produce one topic query plus per-topic lazy selects.
Then use a projection that counts lessons grouped by topic:

```sql
select t.id, t.title, count(l.id)
from topic t left join lesson l on l.topic_id = t.id
group by t.id, t.title
order by t.id;
```

Use a typed DTO projection/repository query and map that to the endpoint.
Acceptance is bounded query count for the result size, correct zero-count topics,
and no entity graph serialized accidentally. Benchmark with representative data;
fewer SQL statements isn't automatically faster if a join explodes row count.
If you add pagination, test page boundaries and counts.

## Lab 6 — Design an outbox for completion events (90 minutes)

**Task (design extension):** Publish `TopicCompleted` only when false/absent becomes
true. Handle crash-before-publish and duplicate-delivery cases.

**Worked solution:**

```text
transaction:
  load current progress
  determine actual false→true transition
  replace progress
  if transitioned: insert outbox UUID + aggregate version + payload
commit
relay:
  claim unsent rows with bounded batch/lease
  publish
  mark sent (a crash can cause duplicate publish)
consumer transaction:
  insert processed_event(event_id) with unique constraint
  if new: apply recommendation effect
commit
```

Tests should simulate: rollback (no outbox), relay down (row retained), repeat same
PUT (no duplicate business transition), publish-before-mark crash (duplicate
transport delivery, one consumer effect), poison message (visible retry/dead-letter
policy), out-of-order versions (explicit consumer decision). Nothing here should
claim that HTTP PUT alone already implements an outbox.

## Lab 7 — Design a production-readiness review (60 minutes)

**Task:** You are asked to expose the demo publicly tomorrow. Write a go/no-go
decision rather than just changing `server.address`.

**Worked answer:** no-go unchanged. Add authenticated identity, ownership-scoped
storage/access, TLS, safe browser session design, server validation and request
limits, dependency maintenance, managed DB/migrations/backups, least privilege,
secret management, deployment probes/resources/graceful shutdown, telemetry and
SLOs. Exercise rollback/restore and failure drills. Replace shared embedded files
before scaling replicas. Existing localhost binding, timeouts, and tests are
useful learning safeguards, not a production security certification.

## Review rubric

Score each dimension 0–2: correct contract, invariant reasoning, failure handling,
appropriate tests, trade-off explanation. A 10/10 solution explains its limits;
adding more frameworks without proving behavior does not earn extra credit.
