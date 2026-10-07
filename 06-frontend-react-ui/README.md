# React.js: Zero to Hero Interview Guide

This chapter is a practical React course for a Java/Spring full-stack developer.
The goal is not to memorize API names. For every concept, you should be able to:

1. Explain the mental model in simple words.
2. Write a small example from a blank file.
3. Connect it to a Spring REST API.
4. Discuss loading, empty, error, accessibility, and performance cases.

The examples use modern React function components and React 19. They are
intentionally small enough to paste into `src/App.jsx`.

> **Interview honesty:** do not claim production React experience you do not
> have. Say that you built this learning dashboard, explain its state ownership,
> async failure handling, tests, and trade-offs, and then answer from first
> principles.

## Contents

- [1. Run the existing project](#1-run-the-existing-project)
- [2. The mental model](#2-the-mental-model)
- [3. JavaScript essentials](#3-javascript-essentials)
- [4. JSX](#4-jsx)
- [5. Components and props](#5-components-and-props)
- [6. State and events](#6-state-and-events)
- [7. Lists, keys, and conditional rendering](#7-lists-keys-and-conditional-rendering)
- [8. Forms and controlled inputs](#8-forms-and-controlled-inputs)
- [9. Sharing state and component design](#9-sharing-state-and-component-design)
- [10. Effects and API calls](#10-effects-and-api-calls)
- [11. Refs](#11-refs)
- [12. Context and reducers](#12-context-and-reducers)
- [13. Custom hooks](#13-custom-hooks)
- [14. Routing](#14-routing)
- [15. Async UI and Spring integration](#15-async-ui-and-spring-integration)
- [16. Performance](#16-performance)
- [17. Error handling and boundaries](#17-error-handling-and-boundaries)
- [18. Portals and reusable UI](#18-portals-and-reusable-ui)
- [19. Testing](#19-testing)
- [20. Accessibility and security](#20-accessibility-and-security)
- [21. TypeScript](#21-typescript)
- [22. Production architecture](#22-production-architecture)
- [23. React 19 topics](#23-react-19-topics)
- [24. Interview questions and answer patterns](#24-interview-questions-and-answer-patterns)
- [25. Practice projects](#25-practice-projects)

---

## 1. Run the existing project

Requirements: Node 22+ (the version is pinned in `package.json`).

```bash
cd 06-frontend-react-ui
npm install
npm run dev
```

Open the URL printed by Vite. Useful commands:

```bash
npm run build       # production build
npm run preview     # serve the production build locally
npm test            # run Vitest once
npm run test:watch  # rerun tests while editing
```

The current app is a study dashboard. Its useful interview examples include:

| File | What to inspect |
| --- | --- |
| `src/App.jsx` | Composition, filtering, controlled inputs, conditional UI |
| `src/TopicCard.jsx` | Props, local draft state, forms, explicit save state |
| `src/useStudy.js` | Custom hook, parallel fetches, aborting stale requests |
| `src/App.test.jsx` | Testing user behavior, failures, and async UI |

To try the small examples below, temporarily replace `src/App.jsx` with one
example and run `npm run dev`. Restore the file afterwards, or use a separate
Vite project:

```bash
npm create vite@latest react-playground -- --template react
cd react-playground
npm install
npm run dev
```

---

## 2. The mental model

React is a library for describing a UI as a function of data:

```text
UI = f(props, state, context)
```

When props or state change, React renders the component again, compares the
result with the previous render, and commits the minimum necessary DOM changes.
The component function is not a one-time constructor; it may run many times.

Important distinctions:

- **Render phase:** React calls components and calculates what the UI should be.
  Do not perform network requests, mutations, or other side effects here.
- **Commit phase:** React applies the required DOM changes.
- **Effect phase:** `useEffect` runs after the commit for synchronization with
  systems outside React (browser APIs, subscriptions, network requests).
- **State is a snapshot:** calling a setter schedules a future render; it does
  not change the current function's `state` variable.
- **Declarative UI:** describe *what* should be visible for a state, rather
  than manually finding and changing DOM nodes.

React is not:

- A database, router, or HTTP client.
- A replacement for JavaScript fundamentals.
- Automatically global state management.
- Automatically fast; unnecessary renders and large bundles are still possible.

---

## 3. JavaScript essentials

React interviews frequently test JavaScript through React questions.

### Destructuring, spread, and immutable updates

```js
const user = { id: 1, name: "Asha", role: "developer" };
const { name, ...withoutName } = user;

const nextUser = { ...user, role: "senior developer" };
const tags = ["react", "java"];
const nextTags = [...tags, "spring"];

// Do not mutate state:
// user.role = "senior developer";
// tags.push("spring");
```

React state should be replaced with a new object/array when it changes. This
makes updates predictable and lets React and memoized components compare
references cheaply.

### Array methods used in JSX

```js
const visibleProducts = products
  .filter((product) => product.inStock)
  .map((product) => product.name);

const total = products.reduce((sum, product) => sum + product.price, 0);
```

### Closures

A function remembers variables from the scope where it was created. Event
handlers and effects use closures, which is why stale values can be a problem:

```js
function createGreeter(prefix) {
  return (name) => `${prefix}, ${name}`;
}

const greet = createGreeter("Hello");
greet("Mina"); // "Hello, Mina"
```

### Promises and `async`/`await`

```js
async function loadUser(id) {
  const response = await fetch(`/api/users/${id}`);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}
```

`fetch` does not reject for HTTP 404/500; check `response.ok` yourself.

### Modules

```js
// format.js
export function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(value);
}

// Product.jsx
import { formatCurrency } from "./format.js";
```

### Interview sentence

> “React uses JavaScript closures heavily. I am careful with immutable updates
> and with values captured by effects or callbacks, because a callback can keep
> an older render's value.”

---

## 4. JSX

JSX is syntax transformed into JavaScript function calls. It is not a string
template and it is not HTML, although it resembles HTML.

```jsx
const name = "Ravi";
const isOnline = true;

export default function Welcome() {
  return (
    <section className="card">
      <h1>Hello, {name}</h1>
      <p>{isOnline ? "Online" : "Offline"}</p>
      <button type="button" aria-label={`Message ${name}`}>
        Message
      </button>
    </section>
  );
}
```

JSX rules:

- Return one root element, a fragment (`<>...</>`), or an array.
- Use `className`, `htmlFor`, `tabIndex`, and camelCase event names.
- Put JavaScript expressions inside `{}`; statements need a helper or
  conditional expression.
- Close every element.
- Components start with an uppercase letter; HTML elements are lowercase.
- Use `style={{ color: "rebeccapurple" }}` for inline styles.
- React escapes text by default. Do not use `dangerouslySetInnerHTML` for
  untrusted content.

Conditional JSX:

```jsx
{isLoading && <Spinner />}
{error ? <ErrorMessage message={error.message} /> : <Results items={items} />}
```

Avoid accidental rendering of zero:

```jsx
// Can render "0" when count is zero:
{count && <Badge />}

// Clear:
{count > 0 && <Badge />}
```

---

## 5. Components and props

A component is a function that accepts props and returns React elements.
Props flow down from parent to child and should be treated as read-only.

```jsx
function Greeting({ name, role = "developer" }) {
  return <p>{name} is a {role}.</p>;
}

export default function App() {
  return (
    <>
      <Greeting name="Asha" />
      <Greeting name="Ravi" role="tech lead" />
    </>
  );
}
```

### `children` and composition

```jsx
function Card({ title, children }) {
  return (
    <article className="card">
      <h2>{title}</h2>
      {children}
    </article>
  );
}

function App() {
  return (
    <Card title="Interview tip">
      <p>Explain the trade-off, not only the definition.</p>
    </Card>
  );
}
```

Composition is usually more flexible than a component with many boolean props.
Prefer `<Modal><CheckoutForm /></Modal>` to a modal that knows every possible
form type.

### Prop drilling

Passing a prop through components that do not use it is prop drilling. First
consider component composition or lifting state. Use context when many
descendants need the same stable concern, such as theme or current user.

### Interview sentence

> “Props are inputs owned by the parent. A child does not mutate a prop; it
> requests a change through a callback. That keeps the source of truth clear.”

---

## 6. State and events

`useState` stores data that affects the UI:

```jsx
import { useState } from "react";

export default function Counter() {
  const [count, setCount] = useState(0);

  function increment() {
    setCount((current) => current + 1);
  }

  return (
    <button type="button" onClick={increment}>
      Clicked {count} times
    </button>
  );
}
```

Use the functional updater when the next value depends on the previous value.
React batches updates, so this is not equivalent:

```js
// Both updates read the same snapshot; often increments only once:
setCount(count + 1);
setCount(count + 1);

// Correct:
setCount((current) => current + 1);
setCount((current) => current + 1);
```

### State is a snapshot

```jsx
function SaveButton() {
  const [status, setStatus] = useState("idle");

  function save() {
    setStatus("saving");
    console.log(status); // "idle" in this render
  }

  return <button onClick={save}>{status}</button>;
}
```

The console reads the value from the render that created `save`. The next
render will see `"saving"`.

### State identity and replacement

```jsx
const [form, setForm] = useState({ email: "", password: "" });

function updateEmail(email) {
  setForm((current) => ({ ...current, email }));
}
```

Do not mutate nested state in place:

```js
// Wrong: mutates the existing object.
form.email = email;
setForm(form);
```

### Events

```jsx
function SearchBox() {
  const [query, setQuery] = useState("");

  return (
    <input
      value={query}
      onChange={(event) => setQuery(event.target.value)}
      onKeyDown={(event) => {
        if (event.key === "Enter") console.log("Search:", query);
      }}
    />
  );
}
```

React event handlers are functions, so pass a function reference:
`onClick={save}`, not `onClick={save()}`.

### Derived state

Do not store values that can be calculated from existing state:

```jsx
const [items, setItems] = useState([]);
const [query, setQuery] = useState("");

const visibleItems = items.filter((item) =>
  item.name.toLowerCase().includes(query.toLowerCase()),
);
```

Storing both `items` and `visibleItems` creates two sources of truth.

---

## 7. Lists, keys, and conditional rendering

```jsx
function TaskList({ tasks }) {
  if (tasks.length === 0) return <p>No tasks yet.</p>;

  return (
    <ul>
      {tasks.map((task) => (
        <li key={task.id}>
          <strong>{task.title}</strong>
          {task.completed && " (done)"}
        </li>
      ))}
    </ul>
  );
}
```

Keys identify an item across renders. Use a stable ID from the data. Avoid an
array index when items can be inserted, removed, or reordered:

```jsx
// Fine only for a static list that never changes order:
items.map((item, index) => <li key={index}>{item.name}</li>)
```

A bad key can make input state appear to move to the wrong row because React
reuses the component instance associated with that key.

### Resetting state with a key

```jsx
<ChatWindow key={selectedContact.id} contact={selectedContact} />
```

Changing the key tells React this is a different identity, so local state is
reset. This is useful for clearing a form when switching records.

---

## 8. Forms and controlled inputs

In a controlled input, React state is the source of truth:

```jsx
import { useState } from "react";

export default function LoginForm() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [submitted, setSubmitted] = useState(false);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    setSubmitted(true);
  }

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Email
        <input
          type="email"
          value={form.email}
          onChange={(event) => update("email", event.target.value)}
          required
        />
      </label>
      <label>
        Password
        <input
          type="password"
          value={form.password}
          onChange={(event) => update("password", event.target.value)}
          minLength={8}
          required
        />
      </label>
      <button type="submit">Sign in</button>
      {submitted && <p role="status">Submitted.</p>}
    </form>
  );
}
```

Controlled vs uncontrolled:

| Approach | Use when |
| --- | --- |
| Controlled (`value` + `onChange`) | Validation, conditional fields, live previews |
| Uncontrolled (`defaultValue` + `ref`) | Simple forms or integrating with non-React code |

Avoid switching an input from uncontrolled to controlled. Initialize text values
to `""`, not `undefined`, if they will later be controlled.

Validate on the client for a good experience, but always validate again in the
Spring backend. Client validation is not a security boundary.

---

## 9. Sharing state and component design

### Lifting state up

If two siblings need to stay synchronized, move their state to the nearest
common parent and pass values/callbacks down:

```jsx
import { useState } from "react";

function TemperatureInput({ value, onChange }) {
  return (
    <label>
      Celsius
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function Converter() {
  const [celsius, setCelsius] = useState("");
  const fahrenheit = celsius === "" ? "" : Number(celsius) * 9 / 5 + 32;

  return (
    <>
      <TemperatureInput value={celsius} onChange={setCelsius} />
      <p>Fahrenheit: {fahrenheit}</p>
    </>
  );
}
```

### State ownership checklist

Ask:

1. Which component needs to read this value?
2. Which component needs to change it?
3. What is the lowest common owner?
4. Can it be derived instead of stored?
5. Is it server state, UI state, or URL state?

Keep state local by default. Global state is not automatically better.

### Server state vs client state

- **Server state:** users, products, orders, cache freshness, request status.
- **Client/UI state:** modal open, selected tab, input draft, dark mode.
- **URL state:** search query, sort, pagination, selected resource.

This separation makes architecture and invalidation decisions easier.

---

## 10. Effects and API calls

An effect synchronizes React with an external system. It is not a general
place to put calculations that could happen during render.

### Basic effect with cleanup

```jsx
import { useEffect, useState } from "react";

function WindowWidth() {
  const [width, setWidth] = useState(window.innerWidth);

  useEffect(() => {
    function handleResize() {
      setWidth(window.innerWidth);
    }

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return <p>Width: {width}px</p>;
}
```

The dependency array means:

- No array: after every commit.
- `[]`: after mount (and cleanup on unmount; development Strict Mode may
  intentionally run setup/cleanup/setup to expose bugs).
- `[roomId]`: when `roomId` changes and on cleanup.

### Fetching data safely

```jsx
function User({ id }) {
  const [state, setState] = useState({
    status: "loading",
    data: null,
    error: null,
  });

  useEffect(() => {
    const controller = new AbortController();

    setState({ status: "loading", data: null, error: null });
    fetch(`/api/users/${id}`, { signal: controller.signal })
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
      })
      .then((data) => setState({ status: "success", data, error: null }))
      .catch((error) => {
        if (error.name !== "AbortError") {
          setState({ status: "error", data: null, error });
        }
      });

    return () => controller.abort();
  }, [id]);

  if (state.status === "loading") return <p>Loading...</p>;
  if (state.status === "error") return <p role="alert">{state.error.message}</p>;
  return <p>{state.data.name}</p>;
}
```

Why abort? If the user changes from ID 1 to ID 2 quickly, the older response
could arrive last and overwrite the newer data. Cleanup cancels the old request.
The current project's `useStudy.js` uses the same principle.

### When not to use an effect

```jsx
// No effect needed: this is a pure calculation.
const fullName = `${firstName} ${lastName}`;

// No effect needed: derive filtered data during render.
const visible = items.filter(matches);
```

Use an effect for subscriptions, timers, browser APIs, network synchronization,
or integrations with systems React does not own.

---

## 11. Refs

A ref stores a mutable value that persists across renders without causing a
render when it changes. It is commonly used to access a DOM node:

```jsx
import { useRef } from "react";

function Search() {
  const inputRef = useRef(null);

  return (
    <>
      <input ref={inputRef} aria-label="Search" />
      <button type="button" onClick={() => inputRef.current?.focus()}>
        Focus search
      </button>
    </>
  );
}
```

Use state for anything visible in the UI. Use refs for focus, measurements,
imperative browser APIs, timer IDs, and previous values.

### Previous value

```jsx
function PreviousValue({ value }) {
  const previous = useRef(value);

  useEffect(() => {
    previous.current = value;
  }, [value]);

  return <p>Previous: {previous.current}</p>;
}
```

Do not read or write refs during render in a way that affects the returned UI.

---

## 12. Context and reducers

### Context

Context avoids passing a value through every intermediate component:

```jsx
import { createContext, useContext, useState } from "react";

const ThemeContext = createContext(null);

function ThemeProvider({ children }) {
  const [theme, setTheme] = useState("light");
  const value = {
    theme,
    toggle: () => setTheme((current) => current === "light" ? "dark" : "light"),
  };

  return <ThemeContext value={value}>{children}</ThemeContext>;
}

function ThemeButton() {
  const { theme, toggle } = useContext(ThemeContext);
  return <button onClick={toggle}>Theme: {theme}</button>;
}
```

Context is dependency injection for a subtree, not a replacement for every
state-management need. Any consumer may rerender when the provider value
changes. Keep provider values focused and stable when necessary.

For React versions before 19, the provider syntax is
`<ThemeContext.Provider value={value}>`.

### `useReducer`

Use a reducer when transitions are related or state is complex:

```jsx
function reducer(state, action) {
  switch (action.type) {
    case "added":
      return {
        ...state,
        todos: [...state.todos, { id: action.id, text: action.text }],
      };
    case "removed":
      return {
        ...state,
        todos: state.todos.filter((todo) => todo.id !== action.id),
      };
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}

function TodoList() {
  const [state, dispatch] = useReducer(reducer, { todos: [] });

  function add(text) {
    dispatch({ type: "added", id: crypto.randomUUID(), text });
  }

  return <p>{state.todos.length} todos</p>;
}
```

A reducer must be pure: same state and action produce the same next state, with
no HTTP calls or mutations inside it.

---

## 13. Custom hooks

A custom hook extracts reusable stateful behavior. It must start with `use` and
may call other hooks:

```jsx
function useDebouncedValue(value, delayMs) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);

  return debounced;
}

function SearchResults({ query }) {
  const debouncedQuery = useDebouncedValue(query, 300);
  return <p>Searching for: {debouncedQuery}</p>;
}
```

Rules of Hooks:

- Call hooks only at the top level, never inside loops, conditions, or handlers.
- Call hooks only from React components or custom hooks.
- Keep a hook's API small and name it after the behavior, not the implementation.

The existing `useStudy` hook is a good example of keeping API reads, progress
state, retry behavior, and persistence out of the page component.

---

## 14. Routing

React itself does not include a router. A common choice is React Router:

```bash
npm install react-router-dom
```

Minimal example:

```jsx
import { BrowserRouter, Link, Route, Routes, useParams } from "react-router-dom";

function ProductPage() {
  const { productId } = useParams();
  return <h2>Product {productId}</h2>;
}

export default function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/">Home</Link>{" "}
        <Link to="/products/42">Product 42</Link>
      </nav>
      <Routes>
        <Route path="/" element={<h1>Home</h1>} />
        <Route path="/products/:productId" element={<ProductPage />} />
        <Route path="*" element={<p>Not found</p>} />
      </Routes>
    </BrowserRouter>
  );
}
```

Interview points:

- Use links instead of full-page `<a href>` navigation for internal routes.
- Route parameters identify a resource; query parameters represent filters,
  sorting, or pagination.
- A protected route is a UI concern; authorization must still be enforced by
  the backend.
- Configure the production server to return `index.html` for client routes.

---

## 15. Async UI and Spring integration

### Model request state explicitly

A robust screen distinguishes at least:

```text
idle -> loading -> success
              \-> error
success -> refreshing -> success/error
```

Also design for empty results, disabled submission, retry, cancellation, and
slow networks. The current dashboard keeps saved progress separate from
unsaved drafts so a failed write does not falsely update the completion count.

### API helper

```js
export async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json", ...options.headers },
    credentials: "include",
    ...options,
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`HTTP ${response.status}: ${body || response.statusText}`);
  }

  return response.status === 204 ? null : response.json();
}
```

### Spring endpoint and React call

Spring:

```java
@GetMapping("/api/products/{id}")
ProductResponse get(@PathVariable long id) {
    return productService.find(id);
}
```

React:

```jsx
async function loadProduct(id, signal) {
  const response = await fetch(`/api/products/${id}`, { signal });
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}
```

Think through the contract:

- JSON field names and date format.
- `201 Created`, `204 No Content`, `400`, `401`, `403`, `404`, `409`, and `500`.
- Pagination shape, for example `{ content, page, size, totalElements }`.
- Validation errors, ideally a stable field-to-message structure.
- CORS in local development and same-origin deployment in production.
- CSRF strategy when using cookie-based sessions.
- Authentication: never put a client secret in a React bundle.

### Vite development proxy

To avoid CORS during local development, proxy `/api` to Spring in
`vite.config.js`:

```js
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: { "/api": "http://localhost:8080" },
  },
});
```

---

## 16. Performance

Start with measurement, not memoization. Use the React DevTools Profiler and
browser Performance panel.

### `memo`, `useMemo`, and `useCallback`

```jsx
import { memo, useCallback, useMemo, useState } from "react";

const ResultRow = memo(function ResultRow({ result, onSelect }) {
  return (
    <button type="button" onClick={() => onSelect(result.id)}>
      {result.name}
    </button>
  );
});

function Results({ results }) {
  const [selected, setSelected] = useState(null);
  const [query, setQuery] = useState("");

  const visible = useMemo(
    () => results.filter((item) => item.name.includes(query)),
    [results, query],
  );
  const select = useCallback((id) => setSelected(id), []);

  return (
    <>
      <input value={query} onChange={(event) => setQuery(event.target.value)} />
      {visible.map((result) => (
        <ResultRow key={result.id} result={result} onSelect={select} />
      ))}
      <p>Selected: {selected}</p>
    </>
  );
}
```

These tools help only when:

- A child is memoized and its props would otherwise change by reference.
- A calculation is measurably expensive.
- A stable callback is needed by a memoized child or an effect.

They add complexity and can make code slower for cheap work. Stable references
alone are not a goal.

Other performance techniques:

- Keep state close to the components that use it.
- Render fewer rows with virtualization for very large lists.
- Lazy-load route-level code with `lazy` and `Suspense`.
- Compress images and use appropriate dimensions/formats.
- Split bundles and avoid importing a huge library for a small function.
- Debounce search requests, but do not debounce controlled input rendering.

### Referential equality

`{}` and `[]` create new references on every render. `React.memo` uses shallow
prop comparison, so a new object prop can invalidate memoization even when its
contents look unchanged.

---

## 17. Error handling and boundaries

HTTP errors belong in request state. Render errors in component trees can be
handled by an error boundary:

```jsx
import { Component } from "react";

export class ErrorBoundary extends Component {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    console.error("UI error", error, info);
  }

  render() {
    if (this.state.hasError) {
      return <p role="alert">This part of the page failed. Refresh to retry.</p>;
    }
    return this.props.children;
  }
}
```

Wrap independent areas when you want one broken widget not to blank the whole
application:

```jsx
<ErrorBoundary>
  <Reports />
</ErrorBoundary>
```

An error boundary does not catch errors in event handlers or ordinary async
callbacks. Handle those explicitly.

`Suspense` handles loading for supported Suspense-enabled APIs and lazy-loaded
code; it is not a universal replacement for every `fetch` effect.

---

## 18. Portals and reusable UI

Portals render a subtree into another DOM node while preserving its React
parentage. They are useful for dialogs, tooltips, and overlays:

```jsx
import { createPortal } from "react-dom";

function Modal({ open, onClose, children }) {
  if (!open) return null;

  return createPortal(
    <div role="dialog" aria-modal="true" className="backdrop">
      <button type="button" onClick={onClose} aria-label="Close">
        Close
      </button>
      {children}
    </div>,
    document.body,
  );
}
```

A production modal also needs focus management, Escape handling, focus return,
scroll locking, and correct labeling. Prefer a well-tested accessible component
library for complex primitives.

Reusable components should have clear contracts:

```jsx
<Button variant="primary" loading={saving} onClick={save}>
  Save
</Button>
```

Avoid a giant “do everything” component. Separate domain components, layout
components, and low-level UI primitives.

---

## 19. Testing

Test user-observable behavior, not implementation details. This project uses
Vitest and Testing Library.

```jsx
import { useState } from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, it } from "vitest";

function Greeting() {
  const [name, setName] = useState("");
  return (
    <>
      <label>
        Name
        <input value={name} onChange={(event) => setName(event.target.value)} />
      </label>
      <p>Hello, {name || "guest"}</p>
    </>
  );
}

it("updates the greeting from user input", async () => {
  const user = userEvent.setup();
  render(<Greeting />);
  await user.type(screen.getByLabelText("Name"), "Mina");
  expect(screen.getByText("Hello, Mina")).toBeInTheDocument();
});
```

Useful test layers:

| Layer | Test |
| --- | --- |
| Unit | Pure formatter, reducer, validation function |
| Component | Form submission, loading/error/empty states |
| Integration | Component with mocked API or service worker |
| End-to-end | Login and checkout through a real browser |

Good tests cover:

- A user action and its visible result.
- Network success, failure, and slow/loading states.
- Keyboard access and accessible names.
- Boundary cases and retry behavior.

Avoid asserting private state, component instance names, or exact DOM structure
unless it matters to the user. Prefer `getByRole`, `getByLabelText`, and
`findBy...` for async results.

Run the repository tests:

```bash
npm test
```

---

## 20. Accessibility and security

### Accessibility checklist

- Use semantic HTML: `button`, `nav`, `main`, `form`, headings.
- Every input has a visible `<label>` or an intentional accessible name.
- Buttons have `type="button"` unless they submit a form.
- Keyboard users can reach and understand every action.
- Focus is visible and managed after navigation or dialogs.
- Use `role="status"` for non-urgent updates and `role="alert"` for errors.
- Do not use color alone to communicate state.
- Test with keyboard, browser accessibility tools, and a screen reader.

```jsx
<label htmlFor="email">Email</label>
<input id="email" name="email" type="email" aria-describedby="email-error" />
{error && <p id="email-error" role="alert">{error}</p>}
```

### Security checklist

- Treat all API responses and user input as untrusted.
- React escapes text by default; avoid `dangerouslySetInnerHTML`.
- Never put secrets, private keys, or backend credentials in frontend code.
- Enforce authorization on the server, not only by hiding a button.
- Use HTTPS and a deliberate cookie/token strategy.
- Understand CSRF for cookies and XSS risks for browser storage.
- Validate uploads and URLs on the backend.

---

## 21. TypeScript

TypeScript catches incorrect prop and state shapes before runtime:

```tsx
type Topic = {
  id: string;
  title: string;
  completed: boolean;
};

type TopicRowProps = {
  topic: Topic;
  onToggle: (id: string) => void;
};

export function TopicRow({ topic, onToggle }: TopicRowProps) {
  return (
    <label>
      <input
        type="checkbox"
        checked={topic.completed}
        onChange={() => onToggle(topic.id)}
      />
      {topic.title}
    </label>
  );
}
```

Useful patterns:

```tsx
type RequestState<T> =
  | { status: "idle" | "loading"; data: null; error: null }
  | { status: "success"; data: T; error: null }
  | { status: "error"; data: null; error: Error };

type ButtonProps = React.ComponentProps<"button"> & {
  variant?: "primary" | "secondary";
};
```

Prefer narrowing discriminated unions over non-null assertions (`!`) and
`any`. Types disappear at runtime, so backend validation is still required.

---

## 22. Production architecture

A practical feature-oriented structure:

```text
src/
  app/
    App.jsx
    routes.jsx
    providers.jsx
  features/
    topics/
      components/
      hooks/
      api.js
      topicReducer.js
  shared/
    ui/
    api/
    formatters/
  test/
```

Possible boundaries:

- **Page/container:** route composition and data loading.
- **Feature component:** domain behavior and user interaction.
- **UI primitive:** generic button, dialog, table, input.
- **API module:** endpoint calls and response mapping.
- **Hook:** reusable stateful behavior, not every utility function.

A request flow should be easy to trace:

```text
route -> page -> feature hook/API -> Spring controller
                         <- DTO / error contract
```

Avoid putting every concern in `App.jsx`, and avoid an abstraction for one
call site. Refactor after repeated behavior is understood.

### Deployment concepts

`npm run build` creates static assets. They can be served by Nginx, a CDN, or a
static hosting service. In a common Spring deployment, either:

- serve the built frontend from the same origin as Spring, or
- deploy frontend and backend separately with explicit CORS and API URLs.

Use environment variables for non-secret configuration such as an API base URL.
Frontend environment variables are bundled into public assets; they are not
secret.

---

## 23. React 19 topics

Know the direction of modern React, but do not use a new API without knowing
the version deployed by the team.

### Actions with `useActionState`

```jsx
import { useActionState } from "react";

async function saveEmail(email) {
  // Replace this with your backend call in a real application.
  await new Promise((resolve) => setTimeout(resolve, 300));
  console.log("Saved:", email);
}

async function submit(previous, formData) {
  const email = formData.get("email");
  if (!email) return { error: "Email is required" };
  await saveEmail(email);
  return { success: true };
}

function NewsletterForm() {
  const [state, formAction, pending] = useActionState(submit, {});

  return (
    <form action={formAction}>
      <input name="email" type="email" />
      <button disabled={pending}>{pending ? "Saving..." : "Save"}</button>
      {state.error && <p role="alert">{state.error}</p>}
    </form>
  );
}
```

Related concepts:

- `useOptimistic` can show an expected result before the server confirms it.
- `useFormStatus` reads the status of a parent form action.
- Server Components and Server Actions depend on the framework, such as
  Next.js; they are not provided by a plain Vite SPA.
- `use` can read supported promises/resources in React 19 environments.

Interview answer:

> “I distinguish React APIs from framework features. A Vite SPA, Next.js
> Server Components, and a Spring-backed browser app have different execution
> boundaries and deployment assumptions.”

---

## 24. Interview questions and answer patterns

### Fundamentals

**What is React?**  
React is a component-based library for declaring UI from props and state. It
rerenders affected component trees and commits required DOM changes. Routing,
data fetching, and state libraries are separate choices.

**What causes a component to rerender?**  
Its state changes, its parent renders and passes new props, a consumed context
value changes, or an external subscription/store notifies it. A rerender does
not necessarily mean the DOM changes.

**Props vs state?**  
Props are read-only inputs owned by the parent. State is mutable data owned by
the component that changes over time and drives its UI.

**Why should state be immutable?**  
Replacing objects creates a clear change boundary, avoids accidental mutation
of other references, and supports predictable rendering and shallow equality.

**Why are keys required?**  
Keys provide stable identity for list items. They let React preserve or reset
the right component instance when a list changes.

### Hooks

**When do you use `useEffect`?**  
When synchronizing with an external system: subscription, timer, browser API,
or network request. I do not use it for values derivable during render.

**Why does an effect run twice in development?**  
Strict Mode intentionally exercises setup and cleanup to expose effects that
are not resilient to remounting. It does not mean production sends two
requests by itself; the effect should still have correct cleanup/idempotency.

**How do you avoid stale API responses?**  
Include the request identity in dependencies, abort the previous request in
cleanup, ignore abort errors, and model loading/error states explicitly.

**`useRef` vs `useState`?**  
State updates render the UI. A ref persists a mutable value without causing a
render and is appropriate for DOM nodes, timers, and imperative handles.

**When would you use context?**  
For a cross-cutting dependency needed by many descendants, such as theme,
locale, or current user. I avoid putting every frequently changing value in a
single context because consumers may rerender together.

### Architecture

**How would you manage a large form?**  
Choose controlled inputs when live validation or dependent fields matter. Keep
draft state local, validate client-side for feedback, submit a DTO, handle
server field errors, disable duplicate submission, and test the failure path.

**How do you design a React/Spring API?**  
Define DTOs and status codes first, keep a shared error shape, check `fetch`
HTTP status explicitly, separate loading from empty and error states, and
handle auth/CSRF/CORS as deployment concerns rather than hiding buttons.

**How do you improve performance?**  
Measure first. Then reduce unnecessary state scope, stabilize only valuable
references, memoize expensive calculations or memoized children, virtualize
large lists, lazy-load routes, and reduce bundle/image cost.

**How do you test React?**  
Test behavior through accessible queries and user events. Cover success,
loading, empty, error, retry, keyboard behavior, and API contracts. Use unit
tests for pure reducers/formatters and end-to-end tests for critical flows.

### A reliable answer structure

For an unfamiliar question, answer in this order:

```text
Definition -> small example -> trade-off -> failure mode -> test
```

Example:

> “A controlled input gets its value from React state. That makes validation
> and dependent fields straightforward, but every keystroke updates state. For
> a large form I would consider an uncontrolled/form-library approach, and in
> either case I would test keyboard submission, server validation, and retry.”

### Questions you should practice aloud

1. Why is `setState` asynchronous from the caller's perspective?
2. Why is an array index a risky key?
3. What is a stale closure?
4. How do you cancel a fetch in an effect?
5. What is the difference between `useMemo` and `useCallback`?
6. When would you lift state up?
7. What is the difference between client state and server state?
8. How do you prevent duplicate form submissions?
9. What happens when a component throws during render?
10. How would you implement pagination and preserve it on refresh?
11. How would you secure a React app with a Spring backend?
12. How do you make a modal accessible?
13. How would you debug a slow React page?
14. What does React Strict Mode do?
15. What would you test for a failed API request?

---

## 25. Practice projects

Build these in increasing difficulty. For each one, write a short design note
and explain it aloud as if you were in an interview.

### Project 1: Todo app

Must have:

- Add, edit, complete, delete, and filter todos.
- Stable keys and immutable updates.
- Empty state and keyboard-accessible form.
- Reducer tests.

### Project 2: Product catalog

Must have:

- Spring `GET /api/products` endpoint.
- Search, category filter, sort, and pagination in URL parameters.
- Loading, empty, error, and retry states.
- Abort stale requests.

### Project 3: Order dashboard

Must have:

- Protected route and current-user endpoint.
- Table, details route, optimistic status update, and rollback on failure.
- Server-side validation and `409 Conflict` handling.
- Component and end-to-end tests.

### Project 4: Interview study planner

This repository already contains one. Improve it by adding:

- URL-persisted filters.
- Topic details route.
- Accessible modal for notes.
- Retry with exponential backoff.
- Pagination or virtualization for a large catalog.
- A Spring integration test for the API contract.

### Definition of “ready”

You are ready to discuss React in an interview when you can build a small
feature without copying a tutorial and explain:

- Where state lives and why.
- Which values are derived.
- What happens during loading, empty, error, and retry.
- How stale requests and duplicate submissions are handled.
- How the UI is tested and made accessible.
- Which work belongs in React, the browser, or Spring.
- What you would measure before optimizing.

The best preparation loop is: **predict -> implement -> run -> break -> fix ->
explain**.
