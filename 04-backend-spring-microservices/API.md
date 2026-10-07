# Study planner HTTP contract

All examples use the default local ports. The browser uses **relative URLs**
and Vite proxies them; service-to-service requests use `CATALOG_BASE_URL`.
Requests/responses are JSON; errors use Spring's RFC 9457 ProblemDetail.
No authentication or learner ID exists. API consumers share one progress set.

## Catalog, port 8081

### `GET /api/topics`

Returns **200** with a JSON array. Stable ascending ID order; no pagination in
this twelve-topic demo. Optional `?track=spring` selects an exact lowercase
track slug. Slugs: `java`, `scala`, `spark`, `spring`, `dsa`, `react`.
A syntactically valid unknown track returns `[]`, not 404.
Track must match `[a-z-]{1,40}`; invalid syntax returns 400.

```json
[
  {
    "id": "spring-transactions",
    "track": "spring",
    "title": "Transactions and persistence",
    "summary": "Trace controller to service to repository; inspect proxy boundaries and isolation.",
    "minutes": 75
  }
]
```

`GET /api/topics?track=spring` actually returns **two** objects; the array above
shows the shape of one item, not the complete filter response.

### `GET /api/topics/{id}`

Returns 200 with one topic object, or 404 if no such topic exists.
ID syntax: `[a-z0-9-]{1,80}`. There is no public create/update/delete catalog API.
The seed inserts missing entries at startup.

## Progress, port 8082

### `GET /api/progress`

Returns **200**, ascending topic ID order:

```json
[
  {
    "topicId": "spring-transactions",
    "completed": true,
    "note": "Explain proxy boundaries without notes"
  }
]
```

`[]` is the legitimate response before any progress is saved. Missing topic
records mean “not started” in the UI. This is **not** a fallback on API failure.
Only successful HTTP reads supply server state. Reads don't call catalog.

### `PUT /api/progress/{topicId}`

Creates or replaces the progress resource; returns **200** for both cases.
This API intentionally does not distinguish created/updated with 201/200.
Returns the canonical saved `topicId`, `completed`, and `note`.

```http
PUT /api/progress/react-effects HTTP/1.1
Content-Type: application/json

{"completed":true,"note":"Cleanup cancels obsolete reads"}
```

- `completed`: required, non-null boolean.
- `note`: optional string, at most 500 Java/JavaScript UTF-16 code units;
  missing/null becomes `""`. This is replacement, not PATCH: omitting note
  clears the old note.
- Topic ID must satisfy the same syntax as catalog IDs.
- Catalog must confirm that the ID exists **on every PUT**.
- Existing progress is unchanged when remote validation fails.
- Repeating the same payload produces the same observable progress state.
  Internal JPA version values are implementation details and not returned.
- `@Version` detects overlapping read-modify-write transactions. It does not
  prevent a later stale browser from replacing a more recent note. There is no
  client ETag/precondition support; sequential writes are last-writer-wins.

### Error table

| Status | Meaning | Caller action |
|---|---|---|
| 400 | Missing/null completed, oversized note, malformed JSON, invalid ID/filter | Correct the request |
| 404 | Catalog cannot find this topic | Reload catalog / correct ID |
| 405 | Unsupported method | Use GET or PUT as documented |
| 409 | Local optimistic-lock/constraint conflict | Reload and reconcile before another PUT |
| 415 | Unsupported content type | Send `application/json` |
| 503 | Catalog timeout/network failure/5xx/unusable response | Keep draft; check service; retry deliberately |
| 5xx other | Unexpected server/proxy failure | Report visibly, inspect logs; don't claim success |

Example body (exact title/validation wording can vary with Spring):

```json
{
  "type": "about:blank",
  "title": "Service Unavailable",
  "status": 503,
  "detail": "Catalog unavailable; progress was not saved. Try again.",
  "instance": "/api/progress/react-effects"
}
```

No stack traces, DB credentials, or remote response bodies are intentionally
included in these explicit errors. Framework-generated errors are not a
versioned application error-code taxonomy; clients key primarily on status.

## Health, timeouts, browser behavior

`GET /actuator/health` on **each** port returns `{"status":"UP"}` when that
service's health indicators (including its DB) are healthy. A progress health
response does **not** promise catalog availability. Only `health,info` are
web-exposed; no public metrics, env, heapdump, or H2 console is enabled.

Progress → catalog uses 1s connect and 2s read timeouts. Browser requests use
a 6s abort timer. Browser cancellation cannot retract a committed server write.
The UI instructs users to reload after an ambiguous timeout.

No permissive CORS configuration is needed: the browser calls the same-origin
Vite proxy. CORS would not authenticate curl or other non-browser clients.
`vite preview` also proxies these paths for **local** build verification.
The built `dist/` alone does not contain an API server or production proxy.

## Manual contract checks

```bash
curl -i 'http://127.0.0.1:8081/api/topics?track=BAD!'
curl -i http://127.0.0.1:8081/api/topics/unknown
curl -i -X PUT http://127.0.0.1:8082/api/progress/react-effects \
  -H 'Content-Type: application/json' -d '{}'
curl -i -X PUT http://127.0.0.1:8082/api/progress/unknown \
  -H 'Content-Type: application/json' -d '{"completed":true}'
```

Expected statuses: 400, 404, 400, 404. To observe 503, stop catalog while keeping
progress running, then PUT a valid-format ID. There is intentionally no
in-process fallback returning “saved” when the catalog cannot be reached.
