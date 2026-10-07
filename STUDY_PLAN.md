# Study plan: from familiarity to interview performance

## 1. Establish a baseline

Spend 75 minutes before starting the course. Do not use search, AI, or solutions.
The result is a diagnostic, not a judgment about your ability.

| Time | Task | Evidence to collect |
| --- | --- | --- |
| 15 minutes | Solve Two Sum and explain a quadratic and a linear-average-time approach | Compiling code, complexity, duplicate and no-answer cases |
| 10 minutes | Explain `equals`/`hashCode`, mutation of map keys, and list/set/map selection | A concrete example for each |
| 10 minutes | Explain why `volatile int count; count++` is not thread-safe | Separate visibility from read-modify-write atomicity |
| 10 minutes | Sketch an API that records completion of a study item | Method, path, request, response, invalid input, duplicate requests |
| 10 minutes | Explain a React effect that loads data when a filter changes | Dependencies, cleanup, stale response handling |
| 10 minutes | Explain a distributed `groupBy` on a highly skewed key | Shuffle, partition ownership, hot task, possible mitigations |
| 10 minutes | Tell a real project story with a trade-off and an outcome | Your contribution separated from your team's contribution |

Score each task from 0 to 3:

- **0:** Could not start or gave an incorrect explanation.
- **1:** Recognized the idea but needed hints or could not finish.
- **2:** Independently correct on the usual case, with gaps in edge cases or trade-offs.
- **3:** Independently correct, handles counterexamples, and communicates clearly.

An average is less useful than your lowest relevant score. A backend candidate with
excellent DSA but no transaction understanding should not skip persistence work.

## 2. Choose a role emphasis

Study all six tracks at least once, but allocate depth according to the job.
These are suggested shares of preparation time, not claims about every interview.

| Role | Java + Scala | Spark + SQL | Backend + design | DSA | React | Behavioral |
| --- | --- | --- | --- | --- | --- | --- |
| Java backend | 20% | 10% | 30% | 25% | 5% | 10% |
| Java full-stack | 15% | 5% | 25% | 20% | 25% | 10% |
| Data engineering | 15% | 35% | 15% | 20% | 5% | 10% |

Read the actual job description. For a Scala-heavy position, increase Scala time.
For senior roles, invest more in design, operational reasoning, and project depth.

## 3. The daily learning loop

Use a 90-minute session as a starting point:

1. **10 minutes: retrieval.** Explain yesterday's ideas from memory before reading notes.
2. **20 minutes: concept.** Read one bounded topic and predict its example's output.
3. **25 minutes: implementation.** Run the example, change a constraint, and explain the change.
4. **25 minutes: unassisted problem.** Attempt a DSA exercise or practical coding task.
5. **10 minutes: review.** Record errors, a counterexample, and the next reattempt date.

If you have 45 minutes, halve the reading and coding scope, not the feedback.
If you have three hours, add a second problem and a mock interview rather than
reading twice as much passively.

Keep a private error log with this template:

```text
Date / topic:
Prompt:
What I predicted:
What happened:
Root misconception:
Minimal counterexample:
Correct invariant or mental model:
Assistance used:
Reattempt dates: +1 day, +3 days, +7 days, +21 days
```

Do not commit personal employer information or confidential interview questions.

## 4. Twelve-week progression

The DSA problem numbers below mean entries in the track catalog's study sequence,
not LeetCode IDs. Move more slowly if exit criteria are not met.

| Week | Primary work | Practical output | Exit criterion |
| --- | --- | --- | --- |
| 1 | Java types, execution, OOP, exceptions; arrays/hash maps | Run and alter core Java examples; attempt first 5 DSA problems | Explain reference/value behavior and derive hash lookup reasoning |
| 2 | Collections, generics, equality; two pointers/sliding windows | Compare data structure choices; next 5 DSA problems | State a window invariant and give an input that breaks a wrong variant |
| 3 | Concurrency, JVM, streams; stacks/binary search | Demonstrate atomicity vs visibility; next 5 DSA problems | Explain happens-before and implement binary search with boundary reasoning |
| 4 | Scala expressions, ADTs, transformations, type classes | Translate a Java transformation into idiomatic Scala; next 5 problems | Explain `map` vs `flatMap`, `Option` vs `Either`, and effectful futures |
| 5 | Spark execution, schemas, joins, windows; SQL foundations | Run both language pipelines and inspect an execution plan; next 5 problems | Identify a shuffle and explain row multiplication in a join |
| 6 | Spark skew, correctness, fault recovery; advanced SQL | Evaluate skew mitigations, write window queries; next 5 problems | Explain retry-safe output and reject unsupported "exactly once" claims |
| 7 | Spring DI, REST, validation, persistence | Run the backend services; map one request through layers; next 5 problems | Distinguish transaction boundary, proxy behavior, and HTTP contract |
| 8 | Service boundaries, failure, observability; graphs | Exercise dependency failure and invalid requests; next 5 problems | Explain local rollback vs remote effects and propose explicit retry semantics |
| 9 | React rendering, hooks, forms, accessibility | Run the connected UI and trace a state update; next 5 problems | Explain stale responses, derived state, keys, and keyboard behavior |
| 10 | Full-stack failures, performance; dynamic programming | Complete remaining 5 DSA problems; explain end-to-end data flow | Derive DP state/transition and distinguish UI success from server persistence |
| 11 | System design, project narratives, mixed drills | Two timed design interviews and two behavioral recordings | Make a quantified, internally consistent design and discuss a real mistake |
| 12 | Mock interview loop and gap repair | Three full mock loops on different days | No critical correctness gaps in the role's core areas; improvement across reattempts |

Finishing 50 problems once is not the goal. Re-solving 15 weak problems independently
is more valuable than immediately adding 50 more.

## 5. Concrete mastery gates

### Java

Explain the difference between:

- Overloading at compile time and overriding at runtime.
- A reference being immutable and the referenced object being immutable.
- An unmodifiable collection view and a defensive immutable snapshot.
- Visibility, atomicity, and mutual exclusion.
- Stream laziness and terminal execution; parallelism and guaranteed speedup.

Then implement an immutable value object used safely as a map key, a bounded
executor task, and a transformation that handles missing values explicitly.

### Scala

Model a successful or failed registration without `null`. Represent the domain
with a case class and a closed alternative set. Compose two dependent validations,
then explain why a plain `map` would create nested containers. Demonstrate a
type class implementation and explain how contextual resolution finds it.

### Spark

Given input rows and a schema, predict the output of a join followed by a window
calculation, including duplicates and nulls. Read the physical plan and distinguish
the logical operation from the chosen execution strategy. Explain what a skewed
partition changes and why simply adding executors may not fix it.

### Backend

Trace a request through routing, validation, service logic, transaction, SQL,
response serialization, and logging. Explain which errors should be 4xx and which
should be 5xx. Show that a dependency outage is not a successful empty response.
Describe a duplicate request strategy without confusing it with optimistic locking.

### DSA

For an unseen problem, clarify constraints, present a baseline, derive a better
approach, state an invariant, implement, and test. Your explanation should survive
"What if there are duplicates?", "What if the input is empty?", and "Does this
mutate the caller's data?" without discovering a new bug each time.

### React

Trace initial render, event handler, state update, rerender, commit, and effect.
Explain why state is a snapshot, why functional updates sometimes matter, and why
an effect must clean up work. Render loading, empty, error, and success states
accessibly. Do not claim `useMemo` prevents all rerenders.

## 6. Weekly mock format

Use a peer if available. Otherwise record your screen and explanation.

| Segment | Time | What the evaluator watches |
| --- | --- | --- |
| Clarification | 5 minutes | Whether you ask about constraints rather than assume them |
| Approach | 5 minutes | Correct baseline, invariant, and justified optimization |
| Coding | 20 minutes | Implementation quality, boundaries, and response to a hint |
| Test and review | 10 minutes | Deliberate adversarial cases rather than one happy path |
| Follow-up | 5 minutes | Ability to adapt when memory, scale, or concurrency changes |

Afterward, choose **one** root weakness to repair. "Practice more" is not a repair
plan; "derive the half-open interval invariant before writing the next three binary
searches" is.

## 7. What to do when time is short

With two weeks left, do not try to read every page at maximum depth. Use the
diagnostic to pick high-value gaps, practice 2-3 representative problems per weak
pattern, run the application, rehearse two project stories, and perform timed mocks.
Keep sleep and breaks in the plan. A tired, memorized answer is less robust than
a rested explanation you can derive.

## 8. Graduation exercise

Without opening a solution, explain and implement a small "study activity" feature:
an API contract, domain model, one persistence operation, a React interaction,
and a daily aggregation design. State what is implemented versus proposed.

Defend validation, duplicate requests, timezone boundaries, concurrency,
authorization, failure handling, and observability. Use the capstone design in
[SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) as a reference only after your first attempt.
