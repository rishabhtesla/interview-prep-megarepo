# Behavioral interviews and project stories

Technical skill is easier to evaluate when you can explain decisions, ownership,
and outcomes. This guide helps you present real experience clearly, not invent it.
If this repository is a personal learning project, call it that. Do not describe
local examples as production systems serving fictional customers.

## 1. Build six evidence-based stories

Prepare one real story for each theme:

| Theme | Evidence to prepare | Common follow-up |
| --- | --- | --- |
| Ownership | What you noticed, initiated, and completed | What would have happened without your intervention? |
| Technical trade-off | Two viable alternatives and a constraint | Why was the rejected option reasonable? |
| Failure or incident | Your mistake, impact, repair, and prevention | What changed in the system, not just your intentions? |
| Conflict | Different goals, listening, evidence, resolution | What did the other person get right? |
| Ambiguity | Unknowns, investigation, staged decisions | Which assumption changed? |
| Learning/mentoring | Initial gap, practice, feedback, transfer | How did you know learning actually happened? |

A single substantial project may provide multiple stories, but do not force every
question into the same rehearsed speech.

## 2. Use STAR plus reflection

**Situation:** Explain only the context needed to understand the problem.

**Task:** State the objective and your responsibility. Distinguish team ownership
from your individual scope.

**Action:** Spend most of the answer here. Explain what you did, why, which
alternatives you considered, and how you collaborated.

**Result:** Use measured evidence if you have it. If you do not have a reliable
metric, say so and describe observable outcomes without manufacturing precision.

**Reflection:** Explain what you would keep, change, or investigate next.

For a two-minute answer, use roughly 20 seconds for context, 15 for your task,
60 for action, and 25 for result/reflection. This is a pacing aid, not a script.

## 3. Honest project narrative for this repository

Fill in the blanks only after doing the work:

```text
I built a local interview-preparation application to learn ______.
The application consists of ______, and my implemented scope was ______.

One decision was ______ rather than ______ because ______.
I demonstrated the trade-off by ______.

A failure I reproduced was ______.
I traced it to ______ using ______.
The repair was ______, and the regression case was ______.

The system currently does not provide ______.
For a real deployment, I would first add ______ because ______.
```

Strong claims are bounded. "The progress update survives a local service restart
using the configured database" is testable. "Highly scalable enterprise-grade
microservices" means little without load, failure, and operational evidence.

## 4. Practice prompts with answer guidance

### "Tell me about yourself"

Give your current engineering focus, one or two relevant examples, and why this
role matches the next kind of problem you want to solve. Avoid reciting every
technology on your resume or giving a long autobiography.

For an early-career candidate, coursework and personal projects are legitimate
examples if described accurately. Explain what you personally implemented.

### "Tell me about a difficult bug"

Choose a bug with a reasoning path, not just an obscure error message. Describe
the symptom, your first hypothesis, the evidence that changed it, the minimal
reproduction, the fix, and the regression protection.

A good learning-project story could involve a stale React response, a mutable Java
map key, or duplicated Spark join output. Only use it if you actually reproduced
and investigated it.

### "Tell me about a disagreement"

Avoid making the other person the villain. Explain the competing constraints.
For example, simplicity and release speed may conflict with operational isolation.
Describe how you gathered evidence, found a reversible step, or escalated a decision
respectfully when agreement was not possible.

### "Tell me about a failure"

Take responsibility without exaggeration. An answer should identify a concrete
decision you could have improved and a concrete change afterward. "I work too hard"
is not a useful failure story. Blaming a teammate while claiming ownership is
internally inconsistent.

### "How do you prioritize?"

Explain impact, urgency, risk, dependencies, and reversibility. Give a real example
of work you deliberately deferred. A list where everything is highest priority
does not demonstrate prioritization.

### "Why microservices?"

Do not imply they are automatically more scalable or professional. Discuss
independent ownership/deployment, domain boundaries, and operational costs. A modular
monolith may be a better choice for a small team. In this repository, separate
services are a learning choice, not proof that the domain requires them.

### "What would you improve in this project?"

Name a limitation, explain its consequence, and propose a prioritized improvement.
For example: authenticated per-user ownership before internet exposure, durable
idempotency before automatic retries, or query measurement before adding a cache.
Do not list every fashionable platform component.

## 5. Project deep-dive worksheet

Be ready to draw the system from memory and answer:

1. What problem does it solve, for whom, and what is deliberately out of scope?
2. Which files contain the entry points, domain logic, persistence, and client state?
3. What happens from a button click through an HTTP request to a stored result?
4. Which invariants are enforced in the UI, API, and database?
5. What happens when the browser disconnects after the server commits?
6. What happens when a dependent service is slow or unavailable?
7. What does the system log, and how would you correlate a failed request?
8. What data could be sensitive, and where should authorization be enforced?
9. What did you measure, and what are you merely estimating?
10. What is the smallest next change that would reduce the largest risk?

For every answer, distinguish **implemented behavior**, **observed behavior**,
and **proposed improvement**.

## 6. Review a recording

Listen for vague language such as "we optimized everything" or "it was scalable."
Replace it with a mechanism and evidence. Notice whether you answered the actual
question in the first 20 seconds or buried it in context.

Score clarity, ownership, reasoning, evidence, and reflection from 0 to 3.
Ask a peer what they still do not understand. Practice the weak transition, not
the whole speech word for word.

## 7. Questions to ask the interviewer

Choose questions you genuinely care about:

- How does the team decide between shipping quickly and investing in reliability?
- What kinds of incidents or performance bottlenecks occupy the team today?
- How are design decisions documented and revisited?
- What does successful ownership look like in the first three to six months?
- How do engineers receive feedback and learn from operational failures?

Do not ask for confidential architecture or sensitive customer details. A useful
question invites discussion about engineering practice rather than fishing for
the "correct" thing to say.
