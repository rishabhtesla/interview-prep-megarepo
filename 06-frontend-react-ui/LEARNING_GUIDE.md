# React and TypeScript learning guide

Use the dashboard as a visual starting point, then read the source in execution
order. The goal is to explain what renders, who owns state, what an effect
synchronizes, and what the user sees when the network fails.

## Setup

```bash
cd 06-frontend-react-ui
npm install
npm run dev
npm test
```

## Lesson order

| Step | Read/inspect | User action | What to explain |
|---:|---|---|---|
| 1 | JSX and component sections in `README.md` | Render the dashboard | Component tree, JSX expressions, composition |
| 2 | `App.jsx` | Filter by track and search | Controlled inputs, derived values, conditional rendering |
| 3 | `TopicCard.jsx` | Edit a note without saving | Local draft state versus confirmed server state |
| 4 | `useStudy.js` and `api.js` | Reload and save progress | Custom hook, async state, API boundary, error propagation |
| 5 | `AlgorithmLab.jsx` | Change target and click Next step | State snapshot, derived steps, visual invariant |
| 6 | React README effects section | Stop a backend service | Loading/error/retry states and abort cleanup |
| 7 | React README forms/accessibility sections | Use keyboard only | Labels, focus, semantic controls, status announcements |
| 8 | `App.test.jsx` and `api.test.js` | Run the tests | User behavior, mocked HTTP, failure-path assertions |
| 9 | React README performance/TypeScript sections | Inspect a rerender | Referential equality, memoization trade-offs, types |

## The browser request trace

```text
user event
-> event handler
-> state update
-> render
-> DOM commit
-> effect / fetch
-> response
-> success or error state
-> accessible status update
```

For every API call, identify:

- Loading state.
- Empty state.
- Success state.
- Failure state.
- Retry behavior.
- Cancellation or stale-response handling.
- Whether unsaved input is preserved.

## Completion criteria

Build a small form from a blank file and explain:

1. Why the input is controlled or uncontrolled.
2. Which component owns its state.
3. Which values are derived.
4. Why an effect has its dependencies and cleanup.
5. How a failed save is announced.
6. How the form is tested through an accessible role or label.
