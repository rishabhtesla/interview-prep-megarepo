# Full-stack learning path: basics to interview-ready

This repository is designed to be used as a sequence, not as unrelated
folders. Spend less time reading and more time producing evidence: a command
that runs, a test that fails before the fix, a diagram you can explain, and a
small feature you can defend.

## How to use every lesson

1. **Predict:** write down what you think the example will print or render.
2. **Run:** use the exact command in that track's learning guide.
3. **Change:** alter one input, failure, or constraint.
4. **Explain:** speak the invariant, ownership, failure mode, and complexity.
5. **Test:** add or run a check that proves the behavior.

Do not move forward because a page looks familiar. Move forward when you can
rebuild the smallest example without copying it.

## Route

| Level | Goal | Start here | Evidence to complete |
| --- | --- | --- | --- |
| 0. Tools | Make the repository runnable | Root README and each track learning guide | Java 17, Maven, Node 22, and the DSA runner work |
| 1. Programming | Build reliable language fundamentals | [`01-core-java/MASTERCLASS.md`](01-core-java/MASTERCLASS.md) | Explain values, references, collections, exceptions, and tests |
| 2. Problem solving | Recognize DSA patterns | `05-dsa-leetcode` | Solve one problem per pattern and state an invariant |
| 3. Data | Query and transform data | [`03-spark-distributed-computing/MASTERCLASS.md`](03-spark-distributed-computing/MASTERCLASS.md), `SQL_AND_DATA_MODELING.md` | Explain joins, indexes, windows, shuffles, and skew |
| 4. Backend | Build a dependable API | [`04-backend-spring-microservices/MASTERCLASS.md`](04-backend-spring-microservices/MASTERCLASS.md) | Trace request → validation → transaction → database → response |
| 5. Frontend | Build an accessible client | `06-frontend-react-ui` | Handle render, state, effects, API failure, and keyboard use |
| 6. Full stack | Connect and operate the system | Backend + frontend | Demonstrate a saved study item and a failed dependency |
| 7. Messaging | Move work across boundaries | `09-messaging-streaming` | Explain retries, ordering, duplicates, state, and dead letters |
| 8. Storage and delivery | Store and ship safely | `10-storage-cloud-devops` | Explain indexes, TTL, containers, probes, IaC, and quality gates |
| 9. GenAI systems | Retrieve evidence and control tools | `11-genai-search-agents` | Explain retrieval, reranking, authorization, tool budgets, and MCP |
| 10. Enterprise integration | Normalize partner boundaries | `12-enterprise-integration` | Explain Camel routing, SOAP safety, discovery, and downstream failure |
| 11. Advanced | Design and defend trade-offs | `SYSTEM_DESIGN.md`, `INTERVIEW_PLAYBOOK.md` | Quantify scale, failure, consistency, security, and observability |

## Suggested 16-week progression

### Weeks 1–2: Java and tools

- Run the zero-to-hero Java tour, then the remaining Java lessons in order.
- Write one immutable value object and one test.
- Explain `equals`/`hashCode`, exceptions, collections, and pass-by-value.
- Do not start Spring before you can read the plain Java examples.

### Weeks 3–5: DSA

- Begin with arrays, strings, hash maps, and two pointers.
- Continue with sliding windows, stacks, binary search, linked lists, trees,
  graphs, heaps, backtracking, and dynamic programming.
- For each problem: brute force, optimized idea, invariant, complexity,
  boundary tests, and a five-minute explanation.
- Use the interactive binary-search lab in the React dashboard to visualize the
  inclusive `[low, high]` invariant.

### Weeks 6–7: SQL, data modeling, and Spark

- Practice joins before reading Spark optimizations.
- Draw one-to-many row multiplication by hand.
- Read a physical plan and identify a shuffle.
- Explain why retries and output idempotency matter in distributed jobs.

### Weeks 8–10: Spring backend

- Run catalog-service and progress-service separately.
- Read the API contract before reading implementation.
- Use the failure lab: stop one service and observe the UI/API response.
- Add one endpoint with validation, a service boundary, persistence, and tests.
- Explain local transactions versus remote calls; do not call a two-service
  operation “one transaction” unless you implemented a distributed protocol.

### Weeks 11–12: React

- Run the dashboard and use its filters, notes, save flow, and Algorithm Lab.
- Read `App.jsx`, `TopicCard.jsx`, and `useStudy.js` in that order.
- Build a small form from scratch.
- Practice loading, empty, error, retry, stale-request, and accessibility cases.
- Read the React track [course](06-frontend-react-ui/COURSE.md) from section 1
  through section 25.

### Weeks 13–14: Full-stack feature

Build a “study activity” feature:

```text
React form
  -> Vite /api proxy
  -> Spring controller + DTO validation
  -> service + transaction
  -> repository + database
  -> JSON response
  -> saved UI state
```

Add a test for success, validation failure, dependency failure, and duplicate
submission. Document what is implemented and what remains a production concern.

### Week 15: Messaging, storage, and delivery

- Run the local messaging simulation and change the retryable failure.
- Explain why an acknowledgement is not a database commit.
- Run the SQL and TTL lessons; read the Docker and Kubernetes artifacts aloud.
- Compare a relational index, Redis TTL, S3 object, and Kafka partition.

### Week 16: GenAI and interview loop

- Run semantic retrieval, graph traversal, the allowlisted agent, and the
  MCP-shaped server.
- Add an authorization filter and explain why similarity is not authorization.
- State which local examples are simulations and which production dependencies
  would replace them.
- Read the Camel/SOAP/discovery lesson and explain where partner contracts,
  registry state, and HTTP timeouts can each fail.
- Two timed DSA sessions each week.
- One backend/API design session.
- One React debugging session.
- One system design session.
- One behavioral story using a real project and an honest contribution boundary.

Use the diagnostic and exit criteria in `STUDY_PLAN.md`; do not measure progress
by the number of pages read.

## Interactive learning surfaces

The repository is deliberately runnable at multiple levels:

- **Java:** executable lessons with assertions.
- **DSA:** standalone Java classes and a strict aggregate runner.
- **Backend:** two independently runnable Spring services with a failure lab.
- **React:** a Vite dashboard with saved progress, filters, async error states,
  and an interactive binary-search visualizer.
- **Cross-track:** the browser calls the backend through a Vite proxy, so you
  can trace a real full-stack request.

If a diagram renderer is unavailable, the Mermaid source remains readable. If a
service is unavailable, prefer the visible error and retry path over silently
mocking success.

## Full-stack interview checklist

Before claiming readiness, answer these without notes:

- How does a browser request reach a Spring controller?
- Where are validation, authorization, and transaction boundaries?
- What happens if the network fails after the database commits?
- How do you prevent stale React responses from overwriting fresh data?
- How do you make a form and modal keyboard accessible?
- How do you choose an index and verify it improves a query?
- Where does a Spark shuffle occur, and how would skew change the plan?
- What is the time/space complexity and invariant of your chosen DSA solution?
- What would you log, measure, alert on, and redact?
- Which parts of your project are implemented versus proposed?
