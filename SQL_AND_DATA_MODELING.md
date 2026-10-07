# SQL and data modeling interview lab

The SQL examples target PostgreSQL syntax. They are standalone exercises, not
migrations for the application's H2 database. No PostgreSQL instance is required
to read the course; execute the examples only in a disposable database you control.
Do not run lab DDL against production or a shared schema.

## 1. Model the domain before writing queries

Use a study platform with users, lessons, current progress, and historical activity.
Current state and history are different models: overwriting a progress row should
not erase the activity history.

```sql
CREATE TABLE learners (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE lessons (
    id BIGINT PRIMARY KEY,
    track TEXT NOT NULL,
    title TEXT NOT NULL
);

CREATE TABLE progress (
    learner_id BIGINT NOT NULL REFERENCES learners(id),
    lesson_id BIGINT NOT NULL REFERENCES lessons(id),
    completed BOOLEAN NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (learner_id, lesson_id)
);

CREATE TABLE activity (
    id BIGINT PRIMARY KEY,
    learner_id BIGINT NOT NULL REFERENCES learners(id),
    lesson_id BIGINT NOT NULL REFERENCES lessons(id),
    occurred_at TIMESTAMPTZ NOT NULL,
    kind TEXT NOT NULL CHECK (kind IN ('view', 'complete', 'reopen'))
);
```

The composite primary key prevents duplicate progress rows even under concurrent
inserts. Application-side "check first, then insert" is not sufficient by itself.
Foreign keys protect referential integrity; they do not enforce per-user authorization.

### Fixture

```sql
INSERT INTO learners VALUES (1, 'Ada'), (2, 'Grace'), (3, 'Linus');
INSERT INTO lessons VALUES
    (10, 'java', 'Collections'),
    (11, 'java', 'Concurrency'),
    (20, 'scala', 'Options');
INSERT INTO progress VALUES
    (1, 10, TRUE,  '2026-01-02T10:00:00Z', 1),
    (1, 11, FALSE, '2026-01-03T10:00:00Z', 2),
    (2, 20, TRUE,  '2026-01-03T11:00:00Z', 1);
INSERT INTO activity VALUES
    (100, 1, 10, '2026-01-02T10:00:00Z', 'complete'),
    (101, 1, 11, '2026-01-03T10:00:00Z', 'view'),
    (102, 1, 11, '2026-01-03T10:00:00Z', 'reopen'),
    (103, 2, 20, '2026-01-03T11:00:00Z', 'complete'),
    (104, 1, 10, '2026-01-05T09:00:00Z', 'view');
```

Tied timestamps are deliberate. Any query promising a single deterministic latest
row must specify a tie-breaker.

## 2. Query exercises and worked answers

Try writing each query before reading its answer.

### Exercise 1: include learners with no completed lessons

Return every learner and their current completed lesson count.

```sql
SELECT l.id, l.name,
       COUNT(p.lesson_id) FILTER (WHERE p.completed) AS completed_count
FROM learners AS l
LEFT JOIN progress AS p ON p.learner_id = l.id
GROUP BY l.id, l.name
ORDER BY l.id;
```

Expected counts: Ada 1, Grace 1, Linus 0.

Why not `COUNT(*)`? The outer join produces a row for Linus with null right-hand
columns, and `COUNT(*)` counts that row. Why not put `p.completed = TRUE` in `WHERE`?
That rejects null-extended rows and removes learners with no completion.

### Exercise 2: learners with no activity

```sql
SELECT l.id, l.name
FROM learners AS l
WHERE NOT EXISTS (
    SELECT 1 FROM activity AS a WHERE a.learner_id = l.id
)
ORDER BY l.id;
```

Expected result: Linus. `NOT EXISTS` directly expresses an anti-join. `NOT IN`
can have surprising behavior when its subquery contains nulls because comparisons
use three-valued logic. This fixture's key is non-null, but production query
changes often invalidate assumptions made around `NOT IN`.

### Exercise 3: latest activity per learner

```sql
WITH ranked AS (
    SELECT a.*,
           ROW_NUMBER() OVER (
               PARTITION BY learner_id
               ORDER BY occurred_at DESC, id DESC
           ) AS rn
    FROM activity AS a
)
SELECT learner_id, id, kind, occurred_at
FROM ranked
WHERE rn = 1
ORDER BY learner_id;
```

Expected activity IDs: 104 for Ada and 103 for Grace. At a cutoff before January 5,
Ada's tied timestamp is resolved by larger ID 102. The tie-breaker is a chosen
deterministic rule; a larger ID does not necessarily prove later real-world occurrence.

`MAX(occurred_at)` alone does not return the other columns from the same row.
Joining back on only the maximum timestamp can return multiple rows.

### Exercise 4: rank learners by completion, preserving ties

```sql
WITH totals AS (
    SELECT l.id,
           COUNT(p.lesson_id) FILTER (WHERE p.completed) AS completed_count
    FROM learners AS l
    LEFT JOIN progress AS p ON p.learner_id = l.id
    GROUP BY l.id
)
SELECT id, completed_count,
       DENSE_RANK() OVER (ORDER BY completed_count DESC) AS completion_rank
FROM totals
ORDER BY completion_rank, id;
```

Ada and Grace both rank 1; Linus ranks 2. `RANK` would produce 1, 1, 3;
`ROW_NUMBER` produces unique positions and needs a tie-breaker for deterministic
assignment. Choose based on product meaning.

### Exercise 5: running daily activity total

```sql
WITH daily AS (
    SELECT (occurred_at AT TIME ZONE 'UTC')::date AS day,
           COUNT(*) AS events
    FROM activity
    GROUP BY (occurred_at AT TIME ZONE 'UTC')::date
)
SELECT day, events,
       SUM(events) OVER (
           ORDER BY day
           ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
       ) AS cumulative_events
FROM daily
ORDER BY day;
```

Expected rows: January 2 -> 1/1, January 3 -> 3/4, January 5 -> 1/5
(daily/cumulative). Missing days are absent. That is fine for cumulative totals,
but matters for calendar-day rolling windows.

### Exercise 6: seven-calendar-day rolling activity

Generate a dense calendar so zero-activity days count as days:

```sql
WITH calendar AS (
    SELECT d::date AS day
    FROM generate_series(
        DATE '2026-01-01', DATE '2026-01-10', INTERVAL '1 day'
    ) AS d
),
daily AS (
    SELECT (occurred_at AT TIME ZONE 'UTC')::date AS day, COUNT(*) AS events
    FROM activity
    GROUP BY (occurred_at AT TIME ZONE 'UTC')::date
),
dense AS (
    SELECT c.day, COALESCE(d.events, 0) AS events
    FROM calendar AS c
    LEFT JOIN daily AS d ON d.day = c.day
)
SELECT day, events,
       SUM(events) OVER (
           ORDER BY day
           ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
       ) AS seven_day_events
FROM dense
ORDER BY day;
```

On January 7 the rolling count is 5; on January 9 it is 4 because January 2
has fallen out of the window. Without a dense calendar, `6 PRECEDING` means six
previous **rows**, not six previous calendar days.

For per-user results, cross join the calendar with the chosen users and partition
the window by learner. Consider the size of that cross product before using it
for millions of users and years of dates.

### Exercise 7: identify duplicate business-state rows

The `progress` primary key already prevents duplicates in the main table.
Suppose a staging import has no constraints:

```sql
SELECT learner_id, lesson_id, COUNT(*) AS copies
FROM staging_progress
GROUP BY learner_id, lesson_id
HAVING COUNT(*) > 1;
```

This query assumes a separate staging table; it is not part of the fixture.
Before deduplicating, define which revision wins. A deterministic ordering by
business version and ingestion identity is better than arbitrarily keeping a row.
Delivery duplicates and legitimate repeated activity are different concepts.

### Exercise 8: completed lessons but no recent activity

```sql
SELECT l.id, l.name
FROM learners AS l
WHERE EXISTS (
    SELECT 1 FROM progress AS p
    WHERE p.learner_id = l.id AND p.completed
)
AND NOT EXISTS (
    SELECT 1 FROM activity AS a
    WHERE a.learner_id = l.id
      AND a.occurred_at >= TIMESTAMPTZ '2026-01-04T00:00:00Z'
)
ORDER BY l.id;
```

Expected result: Grace. The two subqueries represent independent predicates.
A single join across progress and activity may multiply rows and obscure the
intended logic.

### Exercise 9: completion percentage for each track

For learner 1, count completed lessons against all lessons in each track:

```sql
SELECT l.track,
       COUNT(*) AS total_lessons,
       COUNT(p.lesson_id) FILTER (WHERE p.completed) AS completed_lessons,
       ROUND(
           100.0 * COUNT(p.lesson_id) FILTER (WHERE p.completed) / COUNT(*),
           1
       ) AS completion_percent
FROM lessons AS l
LEFT JOIN progress AS p
  ON p.lesson_id = l.id AND p.learner_id = 1
GROUP BY l.track
ORDER BY l.track;
```

Expected percentages: Java 50.0, Scala 0.0. Put the learner condition in `ON`
to preserve lessons without progress. Decimal `100.0` avoids integer-division
truncation. A track with no lessons would require a separate tracks table and a
defined zero-denominator policy.

### Exercise 10: consecutive-day streaks

First deduplicate to one row per learner/day, then group adjacent dates:

```sql
WITH days AS (
    SELECT DISTINCT learner_id,
           (occurred_at AT TIME ZONE 'UTC')::date AS day
    FROM activity
),
islands AS (
    SELECT learner_id, day,
           day - (ROW_NUMBER() OVER (
               PARTITION BY learner_id ORDER BY day
           ))::integer AS streak_group
    FROM days
)
SELECT learner_id, MIN(day) AS start_day, MAX(day) AS end_day,
       COUNT(*) AS streak_days
FROM islands
GROUP BY learner_id, streak_group
ORDER BY learner_id, start_day;
```

Ada has a two-day streak January 2-3 and a one-day streak January 5. Subtracting
consecutive row numbers from consecutive dates produces a constant group key.
This is the gaps-and-islands pattern. Decide the timezone and what counts as
activity before calculating streaks.

## 3. Indexes and query plans

A useful candidate for recent activity queries is:

```sql
CREATE INDEX activity_learner_time_idx
ON activity (learner_id, occurred_at DESC, id DESC);
```

The leading equality column narrows the user; the remaining columns support
ordered recent reads. This does not mean every query will use it or become faster.
On tiny tables, a sequential scan may be cheaper.

Use `EXPLAIN` to inspect a plan. `EXPLAIN (ANALYZE, BUFFERS)` actually executes
the statement; be particularly careful with writes and expensive queries. Compare
estimated vs actual rows, loops, filters, sorts, and I/O. Misestimated cardinality
can lead to poor join choices.

An index consumes space and adds write maintenance. Selectivity, table size,
access pattern, statistics, and returned columns matter. "Indexes are O(log n)"
is an incomplete performance explanation for disk, caching, and range scans.

### Sargability

For a UTC day's activity, prefer a half-open timestamp range:

```sql
SELECT *
FROM activity
WHERE occurred_at >= TIMESTAMPTZ '2026-01-03T00:00:00Z'
  AND occurred_at <  TIMESTAMPTZ '2026-01-04T00:00:00Z';
```

Wrapping the indexed column in a date conversion may require a matching expression
index rather than the ordinary index. For a user's local day, compute correct
UTC boundaries, including daylight-saving transitions; do not assume every local
day is exactly 24 hours.

## 4. Pagination

Offset pagination is simple, but deep offsets require skipping increasing work
and concurrent changes can shift positions. Keyset pagination uses a stable ordered
key:

```sql
SELECT id, occurred_at, kind
FROM activity
WHERE learner_id = :learner_id
  AND (occurred_at, id) < (:cursor_time, :cursor_id)
ORDER BY occurred_at DESC, id DESC
LIMIT 20;
```

This is parameterized pseudocode: bind parameters through your database library.
The timestamp/ID pair is unique in ordering. A public cursor should encode and
validate the query context, not let clients alter authorization filters.

Keyset pagination does not automatically provide a consistent snapshot while data
changes. Define whether newly inserted or edited rows can appear across pages.

## 5. Transactions and concurrency

### Lost update and optimistic locking

Suppose two devices read version 3. Both submit a new completion value.

```sql
UPDATE progress
SET completed = :completed,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE learner_id = :learner_id
  AND lesson_id = :lesson_id
  AND version = :expected_version;
```

One update affects a row and increments the version; the second using the old
version affects zero rows. Treat that as a conflict, not success. Distinguish it
from a nonexistent row as required by the API contract and authorization policy.

Optimistic locking detects concurrent edits. It is not request idempotency:
retrying a request after an ambiguous response needs a separate contract.

### Isolation levels

| Level/concept | Interview point |
| --- | --- |
| Read committed | PostgreSQL statements see committed snapshots; separate statements can observe changes |
| Repeatable read | PostgreSQL provides a stable transaction snapshot, but some cross-row anomalies remain |
| Serializable | Transactions behave as a serial ordering if committed; serialization failures may require retry |
| Row lock | Protects selected rows under the transaction's semantics; avoid inconsistent lock order |

Behavior varies by database. Do not assume all engines implement a named level
identically. Retrying a whole transaction is only safe when external side effects
are excluded or made appropriately idempotent.

### Write skew

Two administrators each see that another administrator is active and independently
deactivate themselves. Each updates a different row, so row-level version checks
alone may allow "at least one active administrator" to be violated.

Options include serializable transactions with retry, locking a common invariant
row, or another schema/constraint design. The point is to identify the cross-row
invariant, not blindly add locks to each modified row.

## 6. Data modeling trade-offs

Normalize authoritative facts when it prevents contradictory updates. Denormalize
read models when a measured access pattern justifies it, and explain how they are
refreshed/reconciled. A copied field needs an ownership and update policy.

Store event time and ingestion time separately when both matter. Store amounts
with currency and rounding policy. Distinguish missing, unknown, empty, and zero
instead of replacing every null with an arbitrary default.

Historical dimensions often need effective-from/effective-to intervals. Joining
historical events to only the current customer region can change old reports
without any event changing.

## 7. Self-check

Explain, without running a query:

1. Why filtering the right table in `WHERE` can undo a left join.
2. Why joining two one-to-many relationships can multiply aggregates.
3. Why a window function preserves rows while `GROUP BY` collapses them.
4. Why latest-row selection needs deterministic tie handling.
5. Why an index is not automatically beneficial on a tiny table.
6. Why uniqueness must be enforced atomically rather than by "check then insert."
7. Why optimistic locking does not solve all cross-row invariants.
8. Why seven previous rows are not necessarily seven calendar days.

For each, invent a minimal counterexample. If you cannot produce one, revisit
the corresponding query or concurrency scenario.
