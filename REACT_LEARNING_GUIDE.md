# Learn React from A to Z with the E2E UI project

This guide is a self-contained React and frontend engineering handbook. It teaches JavaScript, TypeScript, browser fundamentals, React, API/backend communication, security terminology, debugging, system design, and interview communication by using this repository as the classroom. It is designed for someone who wants to build from a blank folder, understand production code, and explain the project confidently in an interview without depending on a collection of courses.

The goal is not to memorize APIs. The goal is to learn how to look at a React application, understand the complete browser-to-backend data flow, build one yourself, make a safe change, and explain the trade-offs clearly in an interview. No document can guarantee an interview result, but this one gives you a structured study path, revision notes, practical exercises, and project-specific talking points.

> **Important:** This is an internal operations application. Do not place credentials, tokens, private keys, or production-only data in source files, commits, screenshots, or browser storage.

## How to use this guide

Do not read all 1,200+ lines passively. Use this loop for every lesson:

1. **Read the idea.** Explain it in your own words before looking at the example.
2. **Type the example yourself.** Do not copy it blindly; the typing is part of the practice.
3. **Predict the result.** Ask what will render, what state changes, and what network request occurs.
4. **Run it.** Use the browser, console, React DevTools, and Network panel.
5. **Break it deliberately.** Remove a dependency, change a key, or return an invalid value and observe the failure.
6. **Repair it.** Explain the root cause, not only the line that made the error disappear.
7. **Complete the checkpoint.** Do not move on while you cannot explain the data flow.

Keep a learning journal with four columns: **concept**, **file**, **experiment**, and **what I learned**. Every new concept should be connected to one file in this repository.

## 1. What you will learn

By the end, you should be able to:

- Read a React application from its entry point to a feature screen.
- Build components with props, state, events, conditional rendering, and lists.
- Explain React re-rendering, identity, keys, and effects.
- Use TypeScript to model component props, form values, API responses, and unknown data.
- Share state with Context without turning every value into global state.
- Build accessible forms with React Hook Form and Zod.
- Fetch, submit, and validate API data with Axios.
- Create reusable UI components with Radix primitives, `class-variance-authority`, and Tailwind.
- Handle authentication, loading, errors, cancellation, persistence, and optimistic UI safely.
- Debug and improve existing features without breaking unrelated workflows.
- Build and ship the Vite application in the correct environment mode.

## 2. Prerequisites and starting from zero

You do **not** need previous React experience. You will learn the web foundations before the React sections. You will learn faster if you have:

- Basic HTML: elements, attributes, forms, labels, buttons, and accessibility.
- Basic CSS: classes, layout, flexbox, responsive design, and the box model.
- Modern JavaScript: `const`, functions, objects, arrays, destructuring, modules, promises, and `async`/`await`.
- Basic TypeScript: interfaces, union types, generics, and type narrowing.
- Git and the terminal.

If any item is unfamiliar, follow the **Foundation track** below before starting Part I. If you already know the foundations, use it as a quick diagnostic rather than skipping it automatically.

### Foundation track: the web before React

#### 2.1 HTML

HTML describes meaning and structure. A form is not merely a group of styled boxes:

```html
<form>
  <label for="environment">Environment</label>
  <input id="environment" name="environment" />
  <button type="submit">Run</button>
</form>
```

Learn these before JSX:

- Document structure: `html`, `head`, `body`, and the root element.
- Text and structure: headings, paragraphs, sections, lists, and links.
- Forms: labels, inputs, selects, buttons, submission, and validation.
- Accessibility: semantic elements, labels, keyboard operation, and focus.
- Attributes versus properties: `class` in HTML becomes `className` in JSX.

**Checkpoint:** Build a static HTML page with a header, navigation, form, table, and accessible error message. Do not use React or CSS frameworks.

#### 2.2 CSS

CSS controls presentation, not application state. Learn:

- Selectors, inheritance, specificity, and the cascade.
- The box model: content, padding, border, and margin.
- Flexbox for one-dimensional layout.
- Grid for two-dimensional layout.
- Responsive design and media queries.
- Focus, hover, disabled, and error states.

This project uses Tailwind, but Tailwind is still CSS. For example, `flex gap-2 p-4` is a compact way to express flex layout, spacing, and padding.

**Checkpoint:** Recreate one small card from `src/components/ui/card.tsx` using plain CSS first, then express the same layout with Tailwind classes.

#### 2.3 JavaScript

Before React, be comfortable with:

```js
const tasks = [
  { id: 1, title: "Learn JSX", completed: false },
  { id: 2, title: "Build a form", completed: true },
];

const openTasks = tasks.filter((task) => !task.completed);
const labels = tasks.map((task) => task.title);
const firstTask = tasks.find((task) => task.id === 1);
```

Learn these JavaScript concepts in order:

1. Values and variables: strings, numbers, booleans, `null`, `undefined`.
2. Objects and arrays.
3. Functions, parameters, return values, and arrow functions.
4. Conditions, loops, and early returns.
5. `map`, `filter`, `find`, `some`, `every`, and `reduce`.
6. Destructuring and spread syntax.
7. Modules: `export` and `import`.
8. Closures and scope.
9. Promises and `async`/`await`.
10. Error handling with `try`, `catch`, and `finally`.
11. Browser APIs: `fetch`, `localStorage`, `sessionStorage`, and events.

**Checkpoint:** Write a plain JavaScript task manager that can add, complete, filter, and serialize tasks to `localStorage`. React will later provide the UI and re-rendering model; the data operations remain JavaScript.

#### 2.4 The browser and HTTP

A React app still runs in a browser. Understand:

```text
URL -> HTTP request -> server response -> JavaScript -> DOM update -> pixels
```

Learn request methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`), status codes (`2xx`, `4xx`, `5xx`), JSON, cookies, CORS, and the difference between client and server responsibility.

Use the browser DevTools:

- **Elements:** inspect the actual DOM and applied CSS.
- **Console:** inspect runtime errors and logs.
- **Network:** inspect URL, method, request body, cookies, status, and response.
- **Application:** inspect storage and cookies.
- **Sources:** set breakpoints and inspect call stacks.

**Checkpoint:** Use `fetch` in a plain HTML page, show a loading message, display JSON data, and show a useful error when the server is unavailable.

### Foundation exit test

You are ready for React when you can explain all of these without searching:

- Why a browser loads `index.html` before `src/main.tsx`.
- Why a `<label>` needs to be associated with an input.
- Why `map` returns a new array and `filter` removes items without mutating the original.
- What a promise represents and why `await` belongs inside an `async` function.
- Where to find a failed request’s status code and response body.
- Why a secret in a `VITE_` environment variable is not secret after bundling.

### Install and run the project

Install the project dependencies before starting:

```bash
npm install
npm run local
```

Open the URL printed by Vite. The `dev-local` mode uses the `.env.dev-local` values and skips authentication when `VITE_SKIP_AUTH=true`. Use only approved local development configuration.

Useful scripts:

```bash
npm run local       # Vite in dev-local mode
npm run dev         # Vite in development mode
npm run prod        # Vite in production mode
npm run wallerd     # Vite in wallerd mode on port 8080
npm run build       # TypeScript build followed by production Vite build
npm run lint        # ESLint
```

## 3. The application map

Start with this dependency path:

```text
index.html
  -> src/main.tsx
      -> AuthProvider
          -> App
              -> ThemeProvider
                  -> Tabs
                      -> feature screens such as Stitch and Observable
                          -> reusable UI components
                          -> services/setupAxios.ts
                              -> backend APIs
```

The most useful files to read in order are:

| File | What it teaches |
| --- | --- |
| `index.html` | The browser document, `#root`, and pre-React loading/theme setup |
| `src/main.tsx` | `createRoot`, JSX entry, a small path-based router, and providers |
| `src/AuthContext.tsx` | Context, session loading, effects, and auth guards |
| `src/App.tsx` | Application composition, tabs, local state, events, and conditional rendering |
| `src/Stitch.tsx` | Typed forms, dynamic task rows, effects, async requests, persistence, and race protection |
| `src/Observable.tsx` | Large form models, field arrays, file inputs, selects, and mode-dependent UI |
| `src/services/auth.ts` | Session probing, redirects, environment flags, and logout |
| `src/services/setupAxios.ts` | An Axios instance, request/response interceptors, cookies, and error propagation |
| `src/components/ui/form.tsx` | React Hook Form context and Radix `Slot` composition |
| `src/components/ui/button.tsx` | Reusable component props, variants, `forwardRef`, and `asChild` |
| `src/components/ui/themecontext.tsx` | A small, well-scoped Context provider and custom hook |
| `src/lib/utils.ts` | `cn`, the class-name merge helper |
| `vite.config.ts` | Environment modes, aliases, plugins, and production chunks |

The app is a single-page Vite application. It does not use React Router: `main.tsx` checks `window.location.pathname` only for `/logout`; the main product navigation is implemented with Radix Tabs in `App.tsx`.

---

# Part I - React fundamentals

## A - Application entry and JSX

React does not start by scanning every component. The browser loads `index.html`, which contains:

```html
<div id="root">
  <div class="initial-loading">Loading Application...</div>
</div>
<script type="module" src="/src/main.tsx"></script>
```

`src/main.tsx` finds that element and asks React to manage it:

```tsx
createRoot(document.getElementById('root')!).render(
  <AppRouter />
)
```

`createRoot` creates a React root. `render` places the component tree into the root. After this point, React owns the contents of `#root`.

JSX looks like HTML, but it is TypeScript/JavaScript syntax:

```tsx
const message = "Loading";
return <div className="initial-loading">{message}</div>;
```

Important JSX rules:

- Use `className`, not `class`.
- JavaScript expressions go inside `{}`.
- Components use PascalCase: `<Header />`.
- A component must return one root element, a fragment, or `null`.
- Close every element: `<Input />` or `<Input></Input>`.
- Event names use camelCase: `onClick`, `onChange`, `onSubmit`.

**Exercise:** Change the initial loading message in `index.html`, then identify why that message disappears when React mounts.

## B - Components

A component is a function that returns a UI description. `ThemeToggle` is a small component:

```tsx
export default function ThemeToggle() {
  const { isDarkMode, toggleTheme } = useTheme();

  return (
    <button onClick={toggleTheme} aria-label="Toggle theme">
      {isDarkMode ? "Light" : "Dark"}
    </button>
  );
}
```

Components should usually have one clear responsibility:

- `App` composes the application.
- `Header` displays the product header.
- `Stitch` owns the Stitch workflow form.
- `Button` standardizes button styling and behavior.
- `ThemeProvider` owns theme state and persistence.

A component function may run many times. Do not treat its function body as a one-time constructor. Values that must survive renders belong in state, refs, context, or an external store.

## C - Props

Props are inputs from a parent to a child. They are read-only from the child’s point of view.

`Stitch` accepts an optional callback:

```tsx
type StitchProps = {
  showToast?: (message: string) => void;
};

const Stitch = ({ showToast = toast }: StitchProps) => {
  // ...
};
```

`App` supplies it:

```tsx
<Stitch showToast={showToast} />
```

This is one-way data flow: the parent owns the callback, and the child calls it when needed.

Use explicit prop types:

```tsx
type StatusCardProps = {
  title: string;
  count: number;
  onRefresh: () => void;
  disabled?: boolean;
};
```

Do not mutate props. If a child needs to change something, notify the parent with a callback or move the state to a shared owner.

## D - State with `useState`

State is data that affects rendering and must survive a re-render:

```tsx
const [isSignOutModalOpen, setIsSignOutModalOpen] = useState(false);
```

The setter schedules a new render:

```tsx
setIsSignOutModalOpen(true);
```

Use the previous-state form when the next value depends on the previous value:

```tsx
setItems((previousItems) => [...previousItems, newItem]);
```

Do not mutate arrays or objects in state:

```tsx
// Wrong
items.push(newItem);
setItems(items);

// Correct
setItems((items) => [...items, newItem]);
```

State belongs at the lowest common owner that needs it. The sign-out modal state belongs in `App`; a selected input value belongs in its feature; theme state belongs in `ThemeProvider`.

**Exercise:** In `App.tsx`, trace the three state transitions for sign-out: `handleSignOut`, `confirmSignOut`, and `cancelSignOut`.

## E - Events and controlled inputs

React event handlers receive browser event objects:

```tsx
const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
  const file = event.target.files?.[0];
  if (file) {
    // validate and store the file
  }
};
```

A controlled input gets its value from React state and reports changes back:

```tsx
const [templateName, setTemplateName] = useState("");

<input
  value={templateName}
  onChange={(event) => setTemplateName(event.target.value)}
/>
```

Controlled inputs are useful when the UI must react immediately to a value. React Hook Form can manage larger forms more efficiently by registering fields and tracking them centrally.

Remember:

- `onClick={handleClick}` passes the function.
- `onClick={handleClick()}` calls it during render and is usually a bug.
- For a parameter, use `onClick={() => handleDelete(id)}`.

## F - Conditional rendering

React renders different trees using JavaScript conditions:

```tsx
if (loading) {
  return <div>Application is loading user session...</div>;
}

return user ? <App /> : <div>Redirecting...</div>;
```

Inline conditions are useful for small branches:

```tsx
{isSignOutModalOpen && <ConfirmationDialog />}
{error ? <FormMessage>{error.message}</FormMessage> : null}
```

Use a named variable or helper when a condition becomes difficult to read. Make loading, empty, error, and success states explicit instead of rendering blank space.

## G - Lists and keys

Lists are rendered with `map`:

```tsx
{tasks.map((task, index) => (
  <TaskRow key={task.id} task={task} />
))}
```

Keys give React stable identity. Prefer a stable domain ID. Do not use an array index when rows can be inserted, removed, or reordered; doing so can attach input state to the wrong row.

React Hook Form’s `useFieldArray` supplies stable field IDs:

```tsx
const { fields, append, remove } = useFieldArray({
  control,
  name: "rows",
});

{fields.map((field, index) => (
  <div key={field.id}>
    {/* rows.${index}.field */}
  </div>
))}
```

## H - Hooks and the Rules of Hooks

Hooks let function components use React features. Common hooks in this project include:

- `useState` for local state.
- `useEffect` for synchronization with external systems.
- `useContext` for shared context.
- `useRef` for DOM nodes and mutable values that do not trigger rendering.
- `useForm`, `useFieldArray`, and `useFormContext` from React Hook Form.

Rules:

1. Call hooks only at the top level of a component or custom hook.
2. Do not call hooks inside loops, conditions, or nested functions.
3. Call hooks only from React components or functions whose names begin with `use`.

The ESLint configuration includes `eslint-plugin-react-hooks` to catch many violations.

## I - Rendering and re-rendering

A state update causes React to call the component again. React compares the new element tree with the previous one and updates only the required DOM nodes.

A re-render does not mean the browser DOM is rebuilt from scratch. It means React recalculates the component output.

Common causes:

- A component’s state changes.
- Its parent re-renders.
- A context value it reads changes.
- Its props change.

Avoid premature memoization. First make the data flow correct. Use `useMemo`, `useCallback`, or `React.memo` only when measurement shows a real performance problem or when stable identity is required by an API.

## J - `useEffect` and external systems

An effect synchronizes React with something outside React: a network request, browser storage, document title, subscription, timer, or third-party widget.

`AuthProvider` probes the session after mounting:

```tsx
useEffect(() => {
  getSession()
    .then((userData) => {
      if (userData) {
        setUser(userData);
        sessionStorage.setItem("user", userData.name);
      }
    })
    .catch(console.error)
    .finally(() => setLoading(false));
}, []);
```

The empty dependency array means this effect is set up after the component mounts. `ThemeProvider` uses an effect to synchronize state with the document and `localStorage`.

Use dependencies honestly:

```tsx
useEffect(() => {
  fetchTemplates(componentName);
}, [componentName]);
```

An effect is not a replacement for a normal event handler. Fetching after a user clicks “Fetch” belongs in the click handler; fetching data needed when a component opens belongs in an effect.

Effects may run more than once in development under Strict Mode. Write them so setup and cleanup are safe and repeatable.

## K - `useRef`

Refs hold a mutable value without causing a render:

```tsx
const fileInputRefs = useRef<{ [key: number]: HTMLInputElement | null }>({});
```

Use refs for:

- Focusing or clicking a DOM element.
- Storing an interval ID.
- Tracking the latest request sequence.
- Keeping a value between renders when changing it should not update the UI.

Do not use refs as a hidden replacement for state. If a value appears in the rendered output, it usually belongs in state.

`Stitch.tsx` uses request sequence refs to ignore stale asynchronous responses. That is a practical example of protecting UI state from race conditions.

## L - Component lifecycle

“Lifecycle” in function components is best understood as synchronization phases:

1. React renders a component.
2. React commits the DOM changes.
3. Effects run.
4. State or props change, causing another render.
5. Effects whose dependencies changed clean up and run again.
6. The component unmounts and its effects clean up.

Do not put side effects directly in render. Render should be deterministic: given the same props, state, and context, it should describe the same UI.

---

# Part II - TypeScript in React

## M - Modeling data with types

Types document the shape of data and make invalid states harder to create:

```tsx
interface User {
  name: string;
  email: string;
}

type Task = {
  component: string;
  version: string;
  weight: number;
};
```

Use `interface` or `type` consistently with the surrounding code. Prefer specific unions over arbitrary strings:

```tsx
type WorkflowMode = "observable" | "mvr" | "ffw" | "dfs";
```

Optional properties represent data that may genuinely be absent:

```tsx
type FormValues = {
  version: string;
  environment: string;
  cron?: string;
};
```

Do not make every property optional to silence compiler errors. Model what the application actually requires.

## N - Event and API types

Type browser events at the boundary:

```tsx
const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
  setValue(event.target.value);
};
```

Type Axios responses when their shape is known:

```tsx
const response = await api.get<string[]>("/orchestrator/versions", {
  params: { component },
});
setData(response.data);
```

Use `unknown` for data from storage, JSON, or external systems until it has been checked. `Stitch.tsx` demonstrates this with `isRecord` and `parseStoredTask`.

Avoid `any`. If a value is unknown, narrow it with checks:

```tsx
const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null;
```

## O - Generic components

React Hook Form components use generics to connect a field name to a form model:

```tsx
const FormField = <
  TFieldValues extends FieldValues,
  TName extends FieldPath<TFieldValues>
>({ ...props }: ControllerProps<TFieldValues, TName>) => {
  return <Controller {...props} />;
};
```

You do not need to write this level of generic code every day, but you should understand the benefit: TypeScript can verify that a field name belongs to the form type.

## P - Type narrowing and errors

Errors from network libraries are not automatically safe to read. Use an explicit guard:

```tsx
if (api.isAxiosError(error)) {
  console.error(error.response?.status);
} else if (error instanceof Error) {
  console.error(error.message);
}
```

The project’s Axios instance exposes `isAxiosError` for this purpose. Always show the user an actionable message and log enough context for diagnosis without logging secrets.

---

# Part III - State, context, and data flow

## Q - Choosing the right state

Use this decision guide:

| Need | Best first choice |
| --- | --- |
| A value used by one component | `useState` |
| A value derived from existing state | Calculate it, or use `useMemo` if expensive |
| A DOM node or mutable non-visual value | `useRef` |
| A value shared by a nearby parent and children | Lift state to the common parent |
| A cross-cutting value such as theme or session | Context |
| Complex transitions in one feature | `useReducer` |
| Server data shared across many screens | A server-state library or a deliberate service/cache layer |

Do not put every form field, modal, and request into Context. Context is most useful for values many descendants need and that do not change on every keystroke.

## R - Context and providers

`AuthContext.tsx` creates a context and provider:

```tsx
const AuthCtx = createContext<AuthState | null>(null);

export const AuthProvider = ({ children }: React.PropsWithChildren) => {
  // load session
  return (
    <AuthCtx.Provider value={{ user, loading }}>
      {children}
    </AuthCtx.Provider>
  );
};
```

Consumers use the custom hook:

```tsx
const { user, loading } = useAuth();
```

Providers must wrap consumers. `main.tsx` wraps the app in `AuthProvider`; `App.tsx` wraps its UI in `ThemeProvider`.

The custom `useTheme` hook deliberately throws if used outside `ThemeProvider`. This fails early instead of silently returning undefined state.

## S - Derived state

Do not store values that can be calculated from other state:

```tsx
const completedCount = tasks.filter((task) => task.status === "done").length;
```

Storing both `tasks` and `completedCount` creates two sources of truth. If the calculation is expensive, use `useMemo`; if it is cheap, calculate it directly.

## T - State machines and reducers

When several booleans describe one process, a state machine or reducer can be clearer than independent state:

```tsx
type RequestState<T> =
  | { status: "idle" }
  | { status: "loading" }
  | { status: "success"; data: T }
  | { status: "error"; message: string };
```

This avoids impossible combinations such as `loading === true` and `error !== null` after a successful response. Existing features use several local booleans; when extending them, consider whether a reducer would make the states easier to reason about.

---

# Part IV - Forms and user input

## U - React Hook Form

The project uses React Hook Form for complex forms:

```tsx
const {
  control,
  register,
  handleSubmit,
  watch,
  setValue,
  reset,
  formState: { errors },
} = useForm<FormValues>({
  defaultValues: {
    version: "",
    environment: "",
    tasks: [{ component: "", version: "", weight: 1 }],
  },
});
```

Use `register` for native inputs:

```tsx
<input {...register("environment", { required: "Choose an environment" })} />
```

Use `Controller` when a third-party or controlled component does not expose a normal `ref`/`name` interface:

```tsx
<Controller
  name="environment"
  control={control}
  render={({ field }) => (
    <ReactSelect {...field} options={environmentOptions} />
  )}
/>
```

Submit through `handleSubmit`:

```tsx
const onSubmit: SubmitHandler<FormValues> = async (values) => {
  // validate, call API, notify, and handle failures
};

<form onSubmit={handleSubmit(onSubmit)}>
  <button type="submit">Run</button>
</form>
```

Always specify button types inside forms. A button without `type` defaults to submit.

## V - Dynamic fields with `useFieldArray`

`Stitch.tsx` and `Observable.tsx` use field arrays for task and row collections:

```tsx
const { fields, append, remove } = useFieldArray({
  control,
  name: "tasks",
});
```

Use the generated `field.id` as the React key. Use the array index only to build the form path (`tasks.${index}.version`), not as the component identity.

## W - Validation and Zod

This repository includes Zod and the React Hook Form resolver. A new feature can centralize validation:

```tsx
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const schema = z.object({
  environment: z.string().min(1, "Environment is required"),
  version: z.string().min(1, "Version is required"),
});

type FormValues = z.infer<typeof schema>;

const form = useForm<FormValues>({
  resolver: zodResolver(schema),
});
```

Keep client validation for user experience, but never treat it as a security boundary. The backend must validate authorization, input, and workflow safety too.

## X - File inputs

File inputs are special: the browser owns the selected file and React cannot set an arbitrary file path. Read the `File` from the change event, validate its type and size, and use `FormData` for upload.

```tsx
const formData = new FormData();
formData.append("file", file);
await api.post("/upload", formData);
```

The Axios request interceptor detects `FormData` and avoids replacing its content type. Do not manually set `Content-Type: multipart/form-data`; the browser needs to add the boundary.

---

# Part V - Effects, APIs, and asynchronous UI

## Y - Fetching data with Axios

Use the configured `api` instance from `src/services/setupAxios.ts`:

```tsx
const response = await api.get<string[]>("/orchestrator/versions", {
  params: { component: "Stitch" },
});
setVersions(response.data);
```

The instance centralizes:

- `baseURL` from `VITE_API_BASE_URL`.
- Cookies through `withCredentials: true`.
- A timeout.
- Request enrichment with the session username.
- Response handling for HTTP 401.

Do not create a new Axios instance in every component. Reuse the configured service so authentication and error behavior remain consistent.

## Z - Loading, success, empty, and error states

Every request should have an explicit UI contract:

```tsx
if (isLoading) return <Spinner />;
if (error) return <ErrorMessage message={errorMessage} />;
if (items.length === 0) return <EmptyState />;
return <ItemList items={items} />;
```

For mutations:

1. Disable the action while it is in progress.
2. Validate input before sending.
3. Show a success or failure notification.
4. Update or refetch affected data.
5. Re-enable the action in a `finally` path.

`sonner` is used for toast feedback. Toasts complement, but do not replace, visible inline errors.

## AA - Authentication flow

Read `src/services/auth.ts` and `src/AuthContext.tsx` together:

1. `main.tsx` renders `AuthProvider`.
2. `AuthProvider` calls `getSession()` in an effect.
3. A valid user is stored in context and `sessionStorage`.
4. An unauthenticated user is redirected to the Okta endpoint unless auth is skipped.
5. `App` reads `user` through `useAuth`.
6. A 401 response in Axios clears the session and calls `logout`.
7. `/logout` renders the standalone `Logout` screen.

The local bypass is controlled by `VITE_SKIP_AUTH`. Never enable a bypass in a production deployment. Authentication in the UI is not authorization; backend endpoints must enforce access independently.

## AB - Environment variables and Vite modes

Vite exposes only variables prefixed with `VITE_` to client code. These values are public after bundling. Never put secrets in them.

The project uses:

- `.env.dev-local` with `vite --mode dev-local`.
- `.env.development` with `vite --mode development`.
- `.env.production` with `vite --mode production`.
- `.env.wallerd` with `vite --mode wallerd`.

Read values through `import.meta.env`:

```tsx
const apiUrl = import.meta.env.VITE_API_BASE_URL;
const mode = import.meta.env.MODE;
```

When adding a variable, update the appropriate environment files and document its purpose. Do not copy production endpoints into local configuration casually.

## AC - Persistence with browser storage

The project uses:

- `sessionStorage` for the signed-in username and the restorable Stitch form.
- `localStorage` for the theme preference.

Storage contains strings:

```tsx
sessionStorage.setItem("stitchFormData", JSON.stringify(formData));
const raw = sessionStorage.getItem("stitchFormData");
```

Treat stored values as untrusted input. Parse inside a `try` block, validate the shape, and remove invalid data. Do not store secrets, access tokens, or sensitive workflow data unnecessarily.

---

# Part VI - Reusable UI and styling

## AD - Composition

Composition means building larger UI from smaller components:

```tsx
<ThemeProvider>
  <Toaster />
  <Header />
  <Tabs>
    <TabsList />
    <TabsContent>
      <Stitch />
    </TabsContent>
  </Tabs>
</ThemeProvider>
```

Prefer composition over large components that know every detail of every feature. When `App.tsx` grows, move tab definitions or screen wrappers into focused components without changing the public behavior.

## AE - Radix and shadcn-style components

The `src/components/ui` directory contains reusable primitives built around Radix UI. These components provide behavior and accessibility while Tailwind provides styling.

Examples:

- `dialog.tsx` for modal behavior and focus management.
- `select.tsx` for accessible select interactions.
- `tabs.tsx` for keyboard-navigable tab panels.
- `form.tsx` for React Hook Form field semantics.
- `button.tsx` for consistent button variants.

Read the component API before replacing a primitive with a plain `<div>`. Accessibility behavior is often the reason the abstraction exists.

## AF - Variants, `forwardRef`, and `asChild`

`button.tsx` demonstrates a reusable variant API:

```tsx
<Button variant="destructive" size="sm">
  Delete
</Button>
```

`class-variance-authority` maps semantic props to Tailwind classes. `cn` combines conditional classes:

```tsx
<Button className={cn(isActive && "ring-2", className)} />
```

`forwardRef` lets a parent or library access the real button element. `asChild` uses Radix `Slot` to apply button behavior to another element. Use it carefully: the child must accept the forwarded props and ref.

## AG - Tailwind and dark mode

Tailwind classes describe styles close to the markup:

```tsx
<div className="flex min-h-0 flex-1 flex-col overflow-y-auto" />
```

This project uses class-based dark mode:

```tsx
<div className="bg-white text-gray-800 dark:bg-black dark:text-white" />
```

`ThemeProvider` toggles the `dark` class on `document.documentElement`, and `tailwind.config.js` declares `darkMode: ["class"]`.

Use semantic UI states and shared primitives where possible. Avoid scattering one-off colors and spacing if a reusable component or design token already exists.

## AH - Accessibility

React does not automatically make a UI accessible. Build accessibility into every feature:

- Use a real `<button>` for actions, not a clickable `<div>`.
- Give every input an associated label.
- Use `aria-label` only when visible text is not possible.
- Preserve keyboard operation for dialogs, tabs, selects, and menus.
- Show validation errors with a useful message and association.
- Do not communicate status through color alone.
- Ensure focus is visible.
- Keep heading structure meaningful.
- Test with keyboard navigation and browser accessibility tools.

`FormControl` in `components/ui/form.tsx` connects generated IDs and `aria-describedby` to validation messages. Follow that pattern for new fields.

---

# Part VII - Architecture and maintainability

## AI - Feature boundaries

Most business screens live directly under `src/` (`Stitch.tsx`, `Observable.tsx`, `Status.tsx`, and others). Shared visual primitives live under `src/components/ui`; integrations live under `src/services` and `src/utils`.

When adding a feature:

1. Define its data types.
2. Keep API calls close to the feature or extract a service when reused.
3. Keep visual primitives in `components/ui`.
4. Keep cross-cutting concerns in providers or services.
5. Keep the feature’s loading, error, empty, and success states visible.
6. Add the screen to `App.tsx` only after it has a clear public component interface.

Avoid importing feature internals into unrelated features. A reusable component should not know that a specific workflow is called Stitch.

## AJ - Custom hooks

Extract a custom hook when logic is reusable or makes a component hard to read:

```tsx
function useStoredJson<T>(
  key: string,
  fallback: T,
  validate: (value: unknown) => value is T
) {
  const [value, setValue] = useState<T>(() => {
    const raw = sessionStorage.getItem(key);
    if (!raw) return fallback;
    try {
      const parsed: unknown = JSON.parse(raw);
      return validate(parsed) ? parsed : fallback;
    } catch {
      return fallback;
    }
  });

  useEffect(() => {
    sessionStorage.setItem(key, JSON.stringify(value));
  }, [key, value]);

  return [value, setValue] as const;
}
```

Only extract logic when the abstraction has a meaningful name and stable responsibility. Do not create a hook solely to hide a few lines.

## AK - Race conditions and cancellation

A user can change a selection while its request is still running. A slower old response can arrive after a newer response and overwrite correct data.

`Stitch.tsx` protects against this with request sequence refs. A simpler request pattern can use `AbortController`:

```tsx
useEffect(() => {
  const controller = new AbortController();

  void api.get("/orchestrator/versions", {
    params: { component },
    signal: controller.signal,
  });

  return () => controller.abort();
}, [component]);
```

Handle cancellation separately from real failures. Do not show an error toast merely because a request was intentionally aborted during cleanup.

## AL - Performance

Start with correct behavior and measure before optimizing. Practical improvements include:

- Keep state near the components that use it.
- Avoid storing duplicate derived state.
- Render only the necessary fields for large dynamic forms.
- Use stable keys.
- Debounce expensive search or validation.
- Lazy-load genuinely large screens when the application grows.
- Avoid passing freshly created objects to memoized children unless needed.
- Use the browser Performance panel and React DevTools Profiler.

The Vite configuration already creates separate vendor chunks for React, React DOM, and Axios. Do not add manual optimization without checking its effect on the built output.

---

# Part VIII - Debugging and quality

## AM - A systematic debugging loop

When a feature misbehaves:

1. Reproduce it with the smallest set of steps.
2. Decide whether the source is render logic, state, an effect, a request, styling, or the backend.
3. Inspect the browser console and Network panel.
4. Trace data from the event handler to state/API to rendered output.
5. Add a temporary, focused log at the boundary—not inside every render.
6. Fix the root cause and remove debug logs.
7. Run the narrowest useful validation.

Examples:

- A button does nothing: check `type`, `onClick`, disabled state, and an exception in the handler.
- A form value resets: check keys, `reset`, default values, and whether the component remounted.
- Old API data appears: check effect dependencies and request cancellation/sequence logic.
- A request is unauthorized: inspect cookies, `VITE_API_BASE_URL`, session state, and the 401 interceptor.
- Dark mode is inconsistent: inspect the root `dark` class and whether the component uses `dark:` classes.

## AN - Type checking, linting, and builds

Run these before handing over a change:

```bash
npm run lint
npm run build
```

`npm run build` runs `tsc -b` and then Vite. TypeScript catches invalid props, missing fields, unsafe values, and many incorrect API assumptions. ESLint catches hook misuse and common JavaScript mistakes.

Use the mode-specific build when the change depends on environment configuration:

```bash
npm run build:local
npm run build:dev
npm run build:prod
npm run build:wallerd
```

## AO - Testing strategy

This repository currently emphasizes linting and build validation. When adding automated tests, prioritize:

- Pure validation functions such as `validateWeights`.
- Storage parsers such as `parseStoredTask`.
- Authentication state transitions.
- Form submission payloads.
- Loading, error, empty, and success rendering.
- API failures and 401 behavior.

Keep business logic in small functions so it can be tested without mounting the entire application. For component tests, verify user-visible behavior rather than implementation details.

---

# Part IX - Guided projects

Complete these in order. Each exercise is deliberately small and uses existing project patterns.

## Project 1: Add a reusable status card

Create a typed `StatusCard` component with `title`, `value`, `description`, and an optional action. Use `Card` primitives and add it to `Status.tsx`.

You will practice:

- Props and TypeScript types.
- Composition.
- Conditional rendering.
- Accessible buttons.
- Tailwind styling.

## Project 2: Add a validated environment form

Create a small form with `environment` and `version`. Use React Hook Form and Zod, show inline errors with the existing form primitives, and display a success toast only after valid submission.

You will practice:

- Schema-driven types.
- `register`.
- `handleSubmit`.
- Error rendering.
- User feedback.

## Project 3: Add an API-backed list

Build a component that calls an existing read-only endpoint through `api`, displays loading/error/empty/success states, and supports refresh.

You will practice:

- Axios response types.
- Async event handlers.
- Request state.
- Error handling.
- Avoiding duplicate Axios configuration.

## Project 4: Add a dynamic task editor

Build a small editor using `useFieldArray`: add, remove, and reorder task rows. Give each row a stable key, validate required fields, and prevent duplicate submissions.

You will practice:

- Nested form paths.
- Field-array identity.
- Controlled third-party inputs.
- Derived validation.
- Disabled/loading actions.

## Project 5: Add a persisted preference

Create a custom hook for one non-sensitive preference stored in `localStorage`. Validate the parsed value and synchronize it with the document or a component.

You will practice:

- Lazy state initialization.
- Effects.
- Storage failure handling.
- Type narrowing.

## Project 6: Improve one existing screen

Choose one feature screen and document its data flow:

```text
user event
  -> handler
      -> state/form update
      -> validation
      -> API request
      -> response state
      -> toast/visible UI
```

Then improve one of its loading, error, empty, accessibility, or race-condition behaviors without changing its intended workflow.

---

# Part X - Reference patterns

## A minimal typed component

```tsx
type GreetingProps = {
  name: string;
  onDismiss?: () => void;
};

export function Greeting({ name, onDismiss }: GreetingProps) {
  return (
    <section aria-labelledby="greeting-title">
      <h2 id="greeting-title">Hello, {name}</h2>
      {onDismiss && (
        <button type="button" onClick={onDismiss}>
          Dismiss
        </button>
      )}
    </section>
  );
}
```

## A request with explicit state

```tsx
type RequestState<T> =
  | { status: "idle" }
  | { status: "loading" }
  | { status: "success"; data: T }
  | { status: "error"; message: string };

const [request, setRequest] = useState<RequestState<string[]>>({
  status: "idle",
});

const loadVersions = async () => {
  setRequest({ status: "loading" });
  try {
    const response = await api.get<string[]>("/orchestrator/versions", {
      params: { component: "Stitch" },
    });
    setRequest({ status: "success", data: response.data });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Request failed";
    setRequest({ status: "error", message });
  }
};
```

## A safe submit handler

```tsx
const [isSubmitting, setIsSubmitting] = useState(false);

const onSubmit = async (values: FormValues) => {
  if (isSubmitting) return;
  setIsSubmitting(true);
  try {
    await api.post("/orchestrator/configs", values);
    toast.success("Saved successfully");
  } catch (error) {
    console.error("Unable to save configuration", error);
    toast.error("Unable to save configuration");
  } finally {
    setIsSubmitting(false);
  }
};
```

## A useful component checklist

Before merging a React change, ask:

- Is the component’s responsibility clear?
- Are props and external data typed?
- Is state stored at the correct owner?
- Are effects synchronized to the correct dependencies?
- Are loading, empty, error, and success states visible?
- Are keys stable?
- Are inputs labeled and keyboard accessible?
- Are API errors propagated and surfaced?
- Could stale responses overwrite newer state?
- Did I avoid secrets and production-only values?
- Does `npm run lint` and `npm run build` pass?

## Final learning path

Read and modify the project in this order:

1. Change a text node in `App.tsx`.
2. Add a prop to a small child component.
3. Add a local state value and a button event.
4. Add conditional loading and empty states.
5. Trace `AuthProvider` and `useAuth`.
6. Trace `ThemeProvider` and `useTheme`.
7. Read `setupAxios.ts` before adding an API call.
8. Add a typed React Hook Form field.
9. Add validation and an inline error.
10. Add a dynamic field-array row.
11. Add persistence with safe parsing.
12. Handle cancellation or stale requests.
13. Extract a reusable component or custom hook.
14. Run lint and the appropriate build mode.

If you can explain the complete path from a click in `Stitch.tsx` to the API request, response handling, state update, toast, and rendered result, you understand the core React architecture of this application.

---

# Part XI - Build React from a blank folder

The existing project is useful for learning architecture, but you should also build a small application without copying its complexity. This track teaches what each layer contributes.

## 11.1 Create the smallest Vite application

To create a separate practice project:

```bash
npm create vite@latest react-practice -- --template react-ts
cd react-practice
npm install
npm run dev
```

Choose `react-ts`, not a framework template, so you can see the browser entry point clearly. A fresh Vite app contains:

```text
index.html
src/
  main.tsx
  App.tsx
  index.css
package.json
tsconfig.json
vite.config.ts
```

Do not add routing, a state library, a UI kit, or an API client on day one. First understand the browser, root, component, state, and event loop.

## 11.2 Build in vertical slices

Do not build “all the backend first” or “all the components first.” Build one complete user-visible slice:

```text
static screen
  -> interaction
      -> local state
          -> validation
              -> fake async request
                  -> success/error UI
```

Each slice teaches more than a large folder of disconnected components.

## 11.3 Step 1: static JSX

Start with a completely static component:

```tsx
export default function App() {
  return (
    <main>
      <h1>Task board</h1>
      <p>Learn React by managing tasks.</p>
      <button type="button">Add task</button>
    </main>
  );
}
```

At this stage, answer:

- Which file is the entry point?
- Which element is mounted by `createRoot`?
- Which function returns JSX?
- Why does clicking the button currently do nothing?

## 11.4 Step 2: state and events

Add state:

```tsx
import { useState } from "react";

export default function App() {
  const [count, setCount] = useState(0);

  return (
    <main>
      <p>Created tasks: {count}</p>
      <button type="button" onClick={() => setCount((value) => value + 1)}>
        Add task
      </button>
    </main>
  );
}
```

Important observations:

- `count` is the value for the current render.
- `setCount` schedules another render.
- The updater form avoids stale state when several updates are queued.
- The click handler is passed, not executed during render.

**Experiment:** Replace the updater form with `setCount(count + 1)` and call the setter three times in one handler. Explain the result before fixing it.

## 11.5 Step 3: model the domain

Create a type before adding more UI:

```tsx
type Task = {
  id: number;
  title: string;
  completed: boolean;
};

const initialTasks: Task[] = [
  { id: 1, title: "Read the React guide", completed: false },
];
```

Use domain language in types. `Task` is more useful than `Record<string, unknown>` because the compiler can verify the fields your UI actually requires.

## 11.6 Step 4: render and update a list

```tsx
const [tasks, setTasks] = useState<Task[]>(initialTasks);

const toggleTask = (id: number) => {
  setTasks((currentTasks) =>
    currentTasks.map((task) =>
      task.id === id
        ? { ...task, completed: !task.completed }
        : task
    )
  );
};

return (
  <ul>
    {tasks.map((task) => (
      <li key={task.id}>
        <label>
          <input
            type="checkbox"
            checked={task.completed}
            onChange={() => toggleTask(task.id)}
          />
          {task.title}
        </label>
      </li>
    ))}
  </ul>
);
```

The update is immutable: it creates a new array and a new object only for the changed task. React can then compare identities predictably.

## 11.7 Step 5: controlled form input

```tsx
const [title, setTitle] = useState("");

const addTask = (event: React.FormEvent<HTMLFormElement>) => {
  event.preventDefault();
  const trimmedTitle = title.trim();
  if (!trimmedTitle) return;

  setTasks((currentTasks) => [
    ...currentTasks,
    {
      id: Date.now(),
      title: trimmedTitle,
      completed: false,
    },
  ]);
  setTitle("");
};

return (
  <form onSubmit={addTask}>
    <label htmlFor="task-title">New task</label>
    <input
      id="task-title"
      value={title}
      onChange={(event) => setTitle(event.target.value)}
    />
    <button type="submit">Add</button>
  </form>
);
```

The input has one source of truth: React state. The form owns submission behavior, and the button explicitly submits the form.

## 11.8 Step 6: split components

When a component becomes difficult to read, split by responsibility:

```tsx
type TaskItemProps = {
  task: Task;
  onToggle: (id: number) => void;
};

function TaskItem({ task, onToggle }: TaskItemProps) {
  return (
    <li>
      <label>
        <input
          type="checkbox"
          checked={task.completed}
          onChange={() => onToggle(task.id)}
        />
        {task.title}
      </label>
    </li>
  );
}
```

The parent keeps the collection and update rule. The child receives data and an event. This is the same parent-to-child pattern used when `App.tsx` supplies `showToast` to `Stitch`.

## 11.9 Step 7: derived filters

Do not store a second list for filtered tasks:

```tsx
const [filter, setFilter] = useState<"all" | "open" | "done">("all");

const visibleTasks = tasks.filter((task) => {
  if (filter === "open") return !task.completed;
  if (filter === "done") return task.completed;
  return true;
});
```

`visibleTasks` is derived from `tasks` and `filter`. Storing it independently would create synchronization bugs.

## 11.10 Step 8: persistence

Persist only non-sensitive data:

```tsx
useEffect(() => {
  localStorage.setItem("practice-tasks", JSON.stringify(tasks));
}, [tasks]);
```

Initialize safely:

```tsx
const [tasks, setTasks] = useState<Task[]>(() => {
  const raw = localStorage.getItem("practice-tasks");
  if (!raw) return initialTasks;

  try {
    const parsed: unknown = JSON.parse(raw);
    if (
      Array.isArray(parsed) &&
      parsed.every(
        (value) =>
          typeof value === "object" &&
          value !== null &&
          "id" in value &&
          "title" in value &&
          "completed" in value
      )
    ) {
      return parsed as Task[];
    }
  } catch {
    // Use the safe initial value below.
  }

  return initialTasks;
});
```

For production code, replace the compact guard with a named type predicate or Zod schema. The key lesson is that storage is external input and must not be trusted.

## 11.11 Step 9: fake asynchronous state

Before connecting a backend, simulate latency:

```tsx
const wait = (milliseconds: number) =>
  new Promise((resolve) => setTimeout(resolve, milliseconds));

const [request, setRequest] = useState<
  "idle" | "saving" | "saved" | "error"
>("idle");

const saveTasks = async () => {
  setRequest("saving");
  try {
    await wait(800);
    setRequest("saved");
  } catch {
    setRequest("error");
  }
};
```

Build the loading, success, and error UI before a real endpoint exists. This prevents the backend from hiding missing UI states.

## 11.12 Step 10: replace the fake request with Axios

Only after the local version works should you add a service:

```tsx
type TasksResponse = {
  tasks: Task[];
};

const response = await api.get<TasksResponse>("/tasks");
setTasks(response.data.tasks);
```

In this repository, use `src/services/setupAxios.ts` rather than a new client. Confirm the endpoint, authentication, request payload, and response shape with the backend contract.

---

# Part XII - A professional learning roadmap

Use this roadmap as a sequence of deliverables. Each stage has a definition of done.

| Stage | Learn | Deliverable | Definition of done |
| --- | --- | --- | --- |
| 0 | HTML, CSS, JavaScript, HTTP, Git | Static task page | It works without React and is keyboard usable |
| 1 | JSX, components, props | Static React dashboard | UI is split into meaningful components |
| 2 | State, events, lists, keys | Interactive task board | Add, complete, remove, and filter work |
| 3 | Forms and validation | Validated editor | Invalid input is blocked and explained |
| 4 | Effects and storage | Persisted board | Reload restores safe non-sensitive state |
| 5 | Async requests | API-backed screen | Loading, error, empty, and success are visible |
| 6 | Context and custom hooks | Theme/session feature | Shared logic has a narrow provider API |
| 7 | UI primitives and accessibility | Reusable design system piece | Keyboard and screen-reader behavior are deliberate |
| 8 | Testing and debugging | Regression coverage | Important user flows are protected |
| 9 | Build and deployment | Production artifact | Correct mode builds and configuration is documented |

### Stage review questions

Before progressing, answer:

- Where is state owned, and why?
- What happens if the network is slow?
- What happens if the network fails?
- What happens if the user clicks twice?
- What happens if the component unmounts during a request?
- What happens if storage contains malformed JSON?
- What happens with an empty list?
- Can the feature be used with only a keyboard?
- Which parts are browser concerns and which are backend concerns?
- Which values are safe to expose in the built JavaScript?

---

# Part XIII - Common mistakes and how to reason about them

## “My state changed but the screen did not update”

Check whether you mutated the existing object or array:

```tsx
// Wrong
task.completed = true;
setTasks(tasks);

// Correct
setTasks((tasks) =>
  tasks.map((task) =>
    task.id === id ? { ...task, completed: true } : task
  )
);
```

React state updates should create a new reference when the value changes.

## “My effect runs forever”

Usually the effect updates a value that is one of its dependencies, or the dependency is recreated on every render:

```tsx
// Suspicious: a new object is created every render.
const options = { component };

useEffect(() => {
  load(options);
}, [options]);
```

Move constants outside the component, depend on primitive values, or use a carefully justified memo:

```tsx
useEffect(() => {
  load({ component });
}, [component]);
```

Do not remove dependencies just to silence ESLint. Understand why the dependency exists.

## “An input loses its value”

Check:

- The component’s `key` did not change unexpectedly.
- The `value` is not switching between `undefined` and a string.
- A `reset` call is not occurring after every render.
- A field-array row uses `field.id`, not a changing index as its React key.

## “I used `useEffect` for everything”

Use an event handler for a user action, a calculation for derived data, and an effect for synchronization with something external. If no external system is involved, an effect may be unnecessary.

## “The API call succeeded but the UI is stale”

After a mutation, decide explicitly whether to:

- Update local state from the response.
- Refetch the affected resource.
- Invalidate a cache.
- Navigate to another screen.

Do not assume that a successful POST automatically updates the list already rendered in memory.

## “The request fires with old data”

Check closures, effect dependencies, and overlapping requests. Use an abort signal or request sequence as shown in `Stitch.tsx`. A request that started earlier is not necessarily the request whose result should win.

## “The browser says CORS”

CORS is a browser policy, not a React error. Inspect:

1. The exact request URL.
2. Whether the backend allows the frontend origin.
3. Whether credentials are required.
4. Whether cookies have compatible `SameSite`, `Secure`, and domain settings.
5. Whether a development proxy is configured.

Do not “fix” CORS by disabling browser security or exposing credentials.

## “The build works locally but not in deployment”

Compare:

- The Vite mode used to build.
- The environment variable names and values.
- The backend base URL.
- Static hosting fallback behavior.
- Whether the build output contains the expected assets.
- Whether a variable was incorrectly treated as a secret.

The code can be correct while the deployed configuration is wrong.

---

# Part XIV - Capstone: build a production-quality operations screen

After completing the smaller projects, build a new screen that fits this application’s architecture without copying an existing feature.

## Capstone brief

Create a **Workflow History** screen that:

- Shows a list of workflow runs.
- Supports loading, empty, error, and success states.
- Filters by environment and status.
- Opens a detail dialog.
- Allows retrying a failed run.
- Uses the existing Axios instance.
- Shows a toast after a successful retry.
- Supports light and dark themes.
- Is usable with keyboard navigation.
- Does not expose credentials or sensitive tokens.

## Suggested domain model

```tsx
type RunStatus = "queued" | "running" | "succeeded" | "failed";

type WorkflowRun = {
  id: string;
  workflow: string;
  environment: string;
  status: RunStatus;
  startedAt: string;
  durationSeconds?: number;
  errorMessage?: string;
};
```

## Capstone implementation order

1. Write the type and static fixture data.
2. Render a table or responsive list.
3. Add typed props to `WorkflowRunRow`.
4. Add status badges with accessible text.
5. Add filter state and derived visible runs.
6. Add a detail dialog using the existing dialog primitive.
7. Add explicit request state.
8. Add a typed `GET` request through `api`.
9. Add retry with double-click protection.
10. Handle a 401 through the existing service behavior.
11. Add an empty state and a retry action after failure.
12. Add keyboard and dark-mode checks.
13. Add the screen to `App.tsx` only after the component is independently understandable.
14. Run `npm run lint` and the relevant build command.

## Capstone acceptance criteria

The screen is complete only when:

- The first render is understandable while data is loading.
- An empty response is not confused with an error.
- A failed request explains what the user can do next.
- A retry cannot accidentally submit twice.
- A stale request cannot overwrite newer filter results.
- Every interactive control has an accessible name.
- Focus moves predictably into and out of the dialog.
- Types describe the API boundary.
- No state is duplicated unnecessarily.
- The feature works in both theme modes.
- The implementation uses existing project conventions instead of adding a competing pattern.

## Capstone review template

Write answers before calling the work finished:

```text
What is the component tree?
Where is each piece of state owned?
Which values are derived?
Which effects synchronize with external systems?
What happens for loading, empty, error, success, and unauthorized responses?
How are stale requests prevented?
How does the UI remain usable without a mouse?
Which files would change if the API response changed?
What would I test first?
What would I monitor after deployment?
```

---

# Part XV - A compact glossary

| Term | Meaning |
| --- | --- |
| Component | A reusable function that returns a description of UI |
| JSX | JavaScript/TypeScript syntax that describes elements and components |
| Props | Read-only inputs passed from a parent to a child |
| State | Data owned by a component that can trigger a re-render |
| Render | React calculating the UI for the current props and state |
| Commit | React applying the calculated changes to the DOM |
| Effect | Synchronization with an external system after commit |
| Ref | A persistent mutable value that does not trigger rendering |
| Context | A way to provide a value to descendants without passing props through every level |
| Hook | A React API or custom function that participates in React behavior |
| Key | Stable identity for an item in a rendered list |
| Controlled input | An input whose value is controlled by React state or a form library |
| Derived data | A value calculated from existing state or props |
| Immutable update | Creating new arrays/objects rather than mutating existing state |
| Client state | UI-owned state such as a dialog or selected tab |
| Server state | Data fetched from and synchronized with a backend |
| Hydration | Attaching React behavior to server-rendered HTML; this Vite SPA does not use SSR hydration |
| Bundle | JavaScript and assets produced for deployment |
| Vite mode | The named build/development configuration used to load environment files |

## The standard you should aim for

A strong React developer can do more than make a component appear on screen. They can explain the browser foundation, choose the right state owner, model data safely, handle every async state, preserve accessibility, debug from evidence, and keep the architecture understandable for the next developer.

Build small things, read the existing code, make predictions, test failure paths, and only then add abstraction. That process is the skill this project is meant to teach.

---

# Part XVI - The complete study path

If you want one document to follow from beginning to interview readiness, use this order. Do not skip the checkpoints; they convert reading into skill.

## Pass 1: understand the web

Study:

1. HTML semantics and forms.
2. CSS layout and responsive design.
3. JavaScript values, functions, arrays, objects, modules, and promises.
4. Browser DevTools and the HTTP request lifecycle.
5. Git basics and npm scripts.

Build: a static task page and a plain JavaScript task manager.

You are ready for Pass 2 when you can explain what the browser does before React runs and can inspect a request in the Network panel.

## Pass 2: learn React by building

Study:

1. JSX and components.
2. Props and one-way data flow.
3. State and immutable updates.
4. Events and controlled inputs.
5. Lists and stable keys.
6. Effects, refs, and context.
7. Forms and validation.

Build: the blank-folder task application in Part XI.

You are ready for Pass 3 when you can add a feature without putting every value in global state or an effect.

## Pass 3: understand this repository

Read in this order:

```text
index.html
  -> src/main.tsx
  -> src/AuthContext.tsx
  -> src/services/auth.ts
  -> src/App.tsx
  -> src/components/ui/tabs.tsx
  -> src/Stitch.tsx
  -> src/services/setupAxios.ts
  -> src/components/ui/form.tsx
  -> src/components/ui/themecontext.tsx
```

For each file, write down:

- Its responsibility.
- Its inputs and outputs.
- Its state.
- Its side effects.
- Its dependencies.
- One possible failure.

You are ready for Pass 4 when you can trace a click in `Stitch.tsx` through validation, Axios, the backend, the response, state, and the rendered result.

## Pass 4: master backend communication

Study Part XVII through Part XX. Practice with browser DevTools, `curl`, and an API client approved by your organization.

Build: the Workflow History capstone with a real or mocked API contract.

## Pass 5: interview revision

Use the quick reference in Part XXI, then answer every question in Part XXII out loud. Do not read the model answer first. Record yourself explaining the architecture in two minutes and in ten minutes.

---

# Part XVII - Backend calls from browser to server

This section explains every layer involved when a user clicks a button that calls a backend.

## 17.1 The request lifecycle

For an authenticated action in this project, the conceptual flow is:

```text
User clicks Run
  -> React event handler executes
  -> form library reads current values
  -> client validation runs
  -> component calls api.post(...)
  -> Axios request interceptor runs
  -> browser attaches eligible cookies
  -> HTTP request travels to the API
  -> server authenticates the session
  -> server authorizes the operation
  -> server validates and deserializes JSON
  -> server performs business logic
  -> server returns status, headers, and body
  -> Axios resolves or rejects
  -> component updates state
  -> React renders feedback
```

React owns the UI portion. The backend owns authentication enforcement, authorization, validation, business rules, persistence, and response correctness. Never rely on the frontend alone for security.

## 17.2 Anatomy of an HTTP request

An HTTP request contains:

```text
METHOD /path?query=value HTTP/1.1
Host: example.internal
Content-Type: application/json
Accept: application/json
Cookie: JSESSIONID=...

{"component":"Stitch","version":"v1"}
```

### Method

The method communicates intent:

| Method | Typical meaning | Safe to retry? | Request body usually |
| --- | --- | --- | --- |
| `GET` | Read a resource | Usually yes | No |
| `POST` | Create or trigger an action | Not automatically | Yes |
| `PUT` | Replace a resource | Often, if designed idempotently | Yes |
| `PATCH` | Partially update a resource | Depends on the operation | Yes |
| `DELETE` | Remove a resource | Often, if designed idempotently | Usually no |

“Safe” means it should not change server state. “Idempotent” means repeating the same request has the same intended result as doing it once. A workflow trigger may be a `POST` because repeating it can start the operation twice.

### URL

A URL can contain:

```text
https://api.example.com/orchestrator/configs?component=Stitch&version=v1
|       |                 |                    |
scheme  host              path                 query string
```

Keep identifiers that select a resource distinct from filters that search it. Follow the backend contract rather than inventing URL conventions in a component.

### Headers

Headers are metadata:

- `Content-Type`: format of the request body.
- `Accept`: formats the client can read.
- `Authorization`: a bearer token or another authorization credential.
- `Cookie`: browser-managed cookies.
- `Origin`: browser origin used by CORS checks.
- `Cache-Control`: caching instructions.
- Correlation/request ID: connects frontend, gateway, and backend logs.

Headers are not automatically secret. A browser user can inspect them. Never log credentials or copy them into documentation.

### Body

The body contains data, commonly JSON:

```json
{
  "component": "Stitch",
  "version": "v1",
  "json": ["{\"environment\":\"dev\"}"]
}
```

JSON has strings, numbers, booleans, arrays, objects, and `null`. It does not have JavaScript `Date`, `Map`, `Set`, `undefined`, or class instances. Convert those values deliberately at the boundary.

## 17.3 Anatomy of an HTTP response

```text
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: JSESSIONID=...; HttpOnly; Secure

{"name":"user@example.com"}
```

A response contains:

- Status code and reason phrase.
- Headers.
- Optional body.

The body can be JSON, text, a file, or empty. Do not assume every successful response is JSON; check the API contract.

## 17.4 Status code terminology

| Code | Name | Frontend meaning |
| --- | --- | --- |
| `200` | OK | Request succeeded with a response body |
| `201` | Created | A resource was created |
| `202` | Accepted | Work was accepted and may finish asynchronously |
| `204` | No Content | Succeeded with no body |
| `301/302/307/308` | Redirect | Client should follow or navigate; behavior depends on method |
| `400` | Bad Request | Malformed request or invalid syntax |
| `401` | Unauthorized | Missing, invalid, or expired authentication |
| `403` | Forbidden | Authenticated but not allowed |
| `404` | Not Found | Resource or endpoint was not found |
| `409` | Conflict | Request conflicts with current state or duplicate operation |
| `422` | Unprocessable Content | Syntax is valid but business validation failed |
| `429` | Too Many Requests | Rate limit; respect retry guidance |
| `500` | Internal Server Error | Backend failed unexpectedly |
| `502` | Bad Gateway | Gateway received an invalid upstream response |
| `503` | Service Unavailable | Service is unavailable or overloaded |
| `504` | Gateway Timeout | Upstream did not respond in time |

Do not show the same message for every status. A 401 may require sign-in; a 403 may require access; a 422 should identify fields; a 503 should offer retry without pretending the operation succeeded.

## 17.5 Axios in this project

`src/services/setupAxios.ts` creates one configured client:

```tsx
const api = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}`,
  withCredentials: true,
  timeout: 15_000,
});
```

This means:

- Components can use relative paths such as `/orchestrator/versions`.
- The base URL is environment-specific.
- Cookies can be included in cross-origin requests when server and browser cookie policies permit it.
- Requests do not wait forever.

The request interceptor reads the session username and adds it to JSON or `FormData`. The response interceptor handles 401 responses, clears the local user, and invokes logout. Interceptors are a cross-cutting mechanism; keep business-specific behavior in the feature that owns it.

Prefer:

```tsx
const response = await api.get<string[]>("/orchestrator/versions", {
  params: { component: "Stitch" },
});
```

over:

```tsx
await axios.get("some-url");
```

The second version bypasses the project’s base URL, credentials, timeout, and interceptors.

## 17.6 Request parameters versus request body

Use query parameters for selection, filtering, sorting, and pagination:

```tsx
api.get("/runs", {
  params: { environment: "dev", status: "failed", page: 1 },
});
```

Use a body for data that creates or changes a resource:

```tsx
api.post("/orchestrator/configs", {
  component: "Stitch",
  version: "v1",
  json: ["..."],
});
```

Use path parameters when identifying one resource:

```text
GET /runs/run-123
```

The exact choice is an API design decision. The frontend should follow the contract consistently.

## 17.7 Serialization and deserialization

Serialization converts an in-memory value into a transport format:

```tsx
const body = JSON.stringify(values);
```

Deserialization converts the response back:

```tsx
const parsed = JSON.parse(response.data[0]);
```

Treat parsed values as `unknown` until validated. The `TemplateFormWrapper` currently parses a saved template and maps fields into the form; when strengthening that code, add a schema or type guard at this boundary.

## 17.8 API contract

An API contract specifies:

- Endpoint and method.
- Authentication requirements.
- Request headers and body.
- Required, optional, and nullable fields.
- Response shape.
- Status codes and error shape.
- Pagination, sorting, and filtering semantics.
- Idempotency and retry behavior.
- Maximum payload/file sizes.

Before writing a frontend request, ask the backend owner for the contract or OpenAPI specification. Never infer that a field is a string merely because one response happened to contain a string.

An example contract table:

| Item | Example |
| --- | --- |
| Method/path | `POST /orchestrator/configs` |
| Auth | Session cookie |
| Request | `component`, `version`, `json` |
| Success | `201` or documented success status |
| Validation failure | `422` with field errors |
| Unauthorized | `401` |
| Conflict | `409` if version already exists |

## 17.9 Async jobs and polling

Operations such as pipeline orchestration may not finish during the initial request. A robust contract may return:

```json
{
  "jobId": "job-123",
  "status": "queued",
  "statusUrl": "/jobs/job-123"
}
```

The frontend should:

1. Show that the job was accepted, not completed.
2. Store the job ID.
3. Poll with a bounded interval or subscribe to a supported event channel.
4. Stop polling on success, failure, cancellation, unmount, or timeout.
5. Allow the user to revisit status if they leave the screen.

Do not keep polling forever, and do not claim success merely because a request returned `202`.

---

# Part XVIII - Backend and security terminology

## 18.1 Authentication versus authorization

These are different:

- **Authentication:** Who are you?
- **Authorization:** What are you allowed to do?

The frontend may know the current user, but the backend must enforce both decisions on every protected operation. Hiding a tab is not authorization.

## 18.2 Session cookies

This project uses a session-oriented pattern:

```text
browser -> login
server -> Set-Cookie: JSESSIONID=...
browser -> sends cookie on eligible requests
server -> finds session and user
```

Important cookie attributes:

- `HttpOnly`: JavaScript cannot read the cookie, reducing token theft through script.
- `Secure`: send only over HTTPS.
- `SameSite`: limits cross-site sending and helps reduce CSRF.
- `Domain` and `Path`: scope where the cookie is sent.
- `Max-Age`/`Expires`: lifetime.

`withCredentials: true` is necessary for Axios to include cookies in eligible cross-origin requests, but it cannot override server CORS or cookie policy.

## 18.3 OAuth 2.0 and OpenID Connect

The auth service redirects to:

```text
/oauth2/authorization/okta
```

Conceptually:

- **OAuth 2.0** is an authorization framework for delegated access.
- **OpenID Connect (OIDC)** adds an identity layer to OAuth 2.0.
- **Identity Provider (IdP)** authenticates the user, such as Okta.
- **Authorization server** issues authorization results/tokens.
- **Client** is the application requesting authentication.
- **Redirect URI** is where the provider returns the browser.
- **Scope** is the requested permission/identity information.
- **Session** is the server-side authenticated state used by subsequent requests.

Do not describe OAuth as “the frontend checks a token.” Explain the actual configured flow: the browser redirects to the provider/backend, a server session is established, and the browser sends the session cookie on later requests.

## 18.4 CSRF

Cross-Site Request Forgery tricks a browser that already has cookies into making an unwanted state-changing request.

Defenses can include:

- SameSite cookies.
- CSRF tokens.
- Checking the `Origin` header.
- Server-side authorization and request validation.
- Avoiding unsafe state changes through `GET`.

If the backend requires a CSRF token, the frontend must obtain and send it according to the contract. Do not invent a token mechanism or disable the protection.

## 18.5 CORS

Cross-Origin Resource Sharing controls whether browser JavaScript may read a response from another origin.

An origin is the combination of:

```text
scheme + host + port
```

For example, `http://localhost:5173` and `http://localhost:8080` are different origins.

For credentialed requests, the server must explicitly allow the requesting origin and credentials. It cannot use a wildcard origin with credentials. CORS is enforced by browsers; server-to-server requests do not use the same browser restriction.

## 18.6 XSS and safe rendering

Cross-Site Scripting executes attacker-controlled script in a trusted page. React escapes ordinary text:

```tsx
<p>{userProvidedText}</p>
```

Be very careful with `dangerouslySetInnerHTML`. If HTML must be rendered, sanitize it with a trusted, maintained strategy and define allowed content. Never interpolate untrusted values into executable script, event attributes, or unsafe URLs.

## 18.7 Secrets and client code

Anything sent to a browser can be inspected. This includes:

- JavaScript bundles.
- `VITE_` environment variables.
- Network requests.
- Source maps when deployed.
- Local storage and session storage.

The client may contain public configuration such as an API base URL. It must not contain private keys, database passwords, client secrets, or service credentials. Put confidential operations behind a trusted backend.

## 18.8 Validation layers

Validate at every relevant boundary:

```text
HTML/browser constraints
  -> React/form validation for immediate feedback
      -> API schema validation
          -> authorization checks
              -> business rule validation
                  -> database constraints
```

Client validation improves usability. Server validation protects the system. Database constraints protect data integrity. None replaces the others.

## 18.9 Logging and observability

Useful request diagnostics include:

- Timestamp.
- Request method and route template.
- Status code.
- Duration.
- Correlation/request ID.
- User or service identity where safe.
- Error category.

Never log passwords, cookies, access tokens, private form contents, or full sensitive payloads. A frontend console log is visible to the user and may be collected by monitoring tools.

---

# Part XIX - Real API debugging playbook

## 19.1 Start at the browser

When a backend call fails:

1. Open DevTools and reproduce once.
2. Find the request in Network.
3. Check the request URL, method, query, payload, and status.
4. Check whether cookies or required headers were sent.
5. Read the response body.
6. Compare the request to the API contract.
7. Check the console for the component’s error path.
8. Check backend logs using the correlation ID if available.

Do not start by changing random React code. First establish whether the request was created correctly and whether the server received it.

## 19.2 A useful `curl` mental model

For a public read endpoint:

```bash
curl -i \
  -H "Accept: application/json" \
  "https://api.example.test/orchestrator/versions?component=Stitch"
```

For JSON data:

```bash
curl -i -X POST \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  --data '{"component":"Stitch","version":"v1","json":["{}"]}' \
  "https://api.example.test/orchestrator/configs"
```

Use placeholders only. Never paste real cookies, tokens, private URLs, or production payloads into a document or shell history.

## 19.3 Error diagnosis table

| Symptom | Likely layer | First evidence |
| --- | --- | --- |
| No request appears | React event/form | Handler, button type, disabled state, console |
| Request has wrong values | Form/state | Request payload and `watch`/state update |
| Request URL is wrong | Vite config/service | `baseURL`, mode, environment variable |
| Browser reports CORS | Browser/server policy | Response headers and origin |
| `401` | Authentication/session | Cookie, session endpoint, auth redirect |
| `403` | Authorization/policy | User permissions and backend audit |
| `404` | Route/version/config | Exact URL and deployed backend |
| `422` | Contract/business validation | Response field errors |
| `409` | Concurrency/duplicate | Existing resource or operation status |
| `500` | Backend | Correlation ID and server logs |
| Request hangs | Network/server/timeout | Timing panel and Axios timeout |
| UI shows old response | Async race/cache | Request order and effect cleanup |

## 19.4 Retry policy

Do not retry every error:

- Retry transient network failures and selected `5xx` responses when the operation is safe.
- Do not blindly retry `POST` operations that trigger work.
- Respect `Retry-After` for `429` or `503` when provided.
- Use exponential backoff with a limit for polling or repeated attempts.
- Tell the user whether an operation may have been accepted even if the response was lost.

Retries are a backend contract concern as much as a frontend concern. Ask whether an idempotency key is supported for repeatable creation or triggering.

## 19.5 Pagination and large responses

Common pagination designs:

```text
page + pageSize
offset + limit
cursor + limit
```

Cursor pagination is often safer for changing datasets because the server returns an opaque cursor for the next page. The frontend must not assume that `page + 1` exists unless the contract says so.

For large responses:

- Request only needed fields if supported.
- Paginate or virtualize large lists.
- Show total counts only when the backend provides a trustworthy count.
- Avoid putting massive response objects into global context.
- Cancel obsolete searches.

## 19.6 Caching

Caching can exist in:

- Browser HTTP cache.
- Service worker.
- CDN or reverse proxy.
- API gateway.
- Backend application cache.
- Database cache.
- Client memory.

Ask:

- How long is data valid?
- What invalidates it after a mutation?
- Is stale data acceptable?
- Can users force refresh?
- Does the response include cache headers or an ETag?

Do not add a custom cache because a request feels slow. First understand the freshness requirement and measure the request.

---

# Part XX - Interview-ready project explanation

## 20.1 The 30-second answer

Adapt this answer to your own experience:

> “This is a TypeScript React single-page application built with Vite. It provides authenticated operational workflows through tabbed feature screens. `main.tsx` mounts the app and supplies authentication context; `App.tsx` composes the header, theme, notifications, and feature tabs. Complex screens such as Stitch and Observable use React Hook Form for typed dynamic forms. A shared Axios instance centralizes the API base URL, credentials, timeout, request enrichment, and 401 handling. Radix/shadcn-style primitives and Tailwind provide accessible reusable UI. The build uses Vite modes for environment-specific configuration.”

Do not claim details you have not verified. Replace “provides” and endpoint names with the exact behavior you can demonstrate.

## 20.2 The two-minute architecture answer

Use this structure:

1. **User entry:** `index.html` provides `#root`; `main.tsx` mounts `AppRouter`.
2. **Authentication:** `AuthProvider` calls `getSession`, exposes `{ user, loading }`, and redirects unauthenticated users unless local auth is skipped.
3. **Composition:** `App` reads auth state, renders the header, sign-out confirmation, theme provider, toaster, and Radix Tabs.
4. **Feature ownership:** each feature screen owns its form state, request state, validation, and user feedback.
5. **Shared integration:** screens import the configured `api`, which uses the environment base URL and credentials.
6. **UI system:** reusable components under `src/components/ui` wrap Radix behavior and Tailwind styles.
7. **Persistence:** theme and selected workflow data use browser storage with different lifetimes.
8. **Delivery:** TypeScript checks the source and Vite emits the environment-specific bundle.

Then give one concrete flow, such as saving a template in `TemplateFormWrapper` or loading versions in `Stitch`.

## 20.3 The ten-minute deep dive

For a deeper interview, explain:

- Why state is local to feature screens rather than all in Context.
- Why `withCredentials` matters for a session cookie.
- How a 401 travels through the Axios response interceptor.
- Why form arrays need stable keys.
- How `Stitch` prevents stale responses from replacing newer selections.
- Why `VITE_` values cannot hold secrets.
- What the UI does when an operation returns `202 Accepted`.
- How you would test a form submit and a failed API request.
- What you would change if the application grew into many independently routed features.

## 20.4 Strong answer structure

For technical questions, answer in this order:

1. **Definition:** what the concept means.
2. **Mechanism:** how it works.
3. **Project example:** where it appears here.
4. **Trade-off:** when it is useful and when it is not.
5. **Failure mode:** what can go wrong and how you handle it.

Example:

> “Context provides a value to descendants without passing it through every component. React reads the provider value and re-renders consumers when it changes. This project uses it for auth and theme, which are cross-cutting concerns. I would not put rapidly changing form fields there because it broadens re-renders and makes ownership unclear. I would also ensure the custom hook fails clearly when used outside its provider.”

## 20.5 STAR stories to prepare

Prepare one real story for each:

- A difficult bug and how you isolated the layer.
- A race condition or stale-data problem.
- A form or validation challenge.
- An accessibility improvement.
- A performance improvement backed by measurement.
- A production configuration or deployment issue.
- A disagreement about architecture and how you resolved it.
- A security or privacy concern you caught.

Use:

```text
Situation: What was happening and who was affected?
Task: What were you responsible for?
Action: What did you inspect, change, and why?
Result: What improved? Include evidence if available.
Learning: What would you do earlier next time?
```

---

# Part XXI - Frontend and backend quick reference

## React quick reference

| Concept | One-line answer |
| --- | --- |
| Component | Function that returns UI from inputs |
| Props | Read-only parent-to-child inputs |
| State | Component-owned data that triggers rendering |
| Hook | API for using React behavior in function components |
| Effect | Synchronization with something outside React |
| Ref | Persistent mutable value that does not render |
| Key | Stable list-item identity |
| Context | Descendant access to a shared value |
| Controlled input | Input value owned by React/form state |
| Derived state | Calculated value that should not be stored separately |
| Immutable update | New array/object reference instead of mutation |
| Lifting state | Moving state to the nearest common owner |
| Reconciliation | React comparing element trees to update the DOM |
| Strict Mode | Development checks that expose unsafe patterns |

## JavaScript and TypeScript quick reference

| Concept | Interview description |
| --- | --- |
| Closure | Function retaining access to variables from its lexical scope |
| Promise | Object representing eventual async completion or failure |
| `async/await` | Syntax for composing promise-based code |
| `map` | Transforms each array item into a new array |
| `filter` | Returns items passing a predicate |
| `reduce` | Accumulates an array into a value |
| `unknown` | Safe external value requiring narrowing |
| `any` | Disables type safety; avoid at boundaries |
| Union | Value can be one of several types |
| Generic | Type parameter reusable across different concrete types |
| Type guard | Runtime check that narrows a TypeScript type |
| Structural typing | Compatibility based on shape |

## HTTP and backend quick reference

| Term | Meaning |
| --- | --- |
| Endpoint | Method plus URL a client can call |
| Resource | Backend entity represented by an API |
| Payload | Data sent in a request or returned in a response |
| Header | Request/response metadata |
| Cookie | Browser-managed key/value sent according to policy |
| Session | Server-side state associated with a client |
| Token | Credential or signed data used to represent access/identity |
| Authentication | Establishing who the caller is |
| Authorization | Deciding what the caller may do |
| CORS | Browser policy for reading cross-origin responses |
| CSRF | Abuse of ambient browser credentials for unwanted actions |
| Serialization | Converting values to a transport format |
| Idempotency | Repeating an operation has the same intended effect |
| Pagination | Splitting a collection across responses |
| Rate limiting | Restricting request frequency |
| Timeout | Maximum wait before giving up |
| Retry | Attempting a failed operation again under defined rules |
| Correlation ID | Identifier connecting logs across services |
| Contract | Agreed request, response, error, and behavior shape |

## Security quick reference

| Term | Remember |
| --- | --- |
| XSS | Escape/sanitize untrusted content; avoid unsafe HTML |
| HTTPS | Encrypts traffic in transit; it does not authorize users |
| HttpOnly | Prevents JavaScript from reading a cookie |
| Secure cookie | Sent only over HTTPS |
| SameSite | Controls cross-site cookie sending |
| Least privilege | Give identities only required access |
| Secret | Never ship it to the browser |
| Defense in depth | Use multiple independent protections |

---

# Part XXII - Interview questions with answer guides

Answer each question aloud before expanding the answer. The answer guide tells you what a strong answer should contain; it is not a script to memorize.

## React fundamentals

### 1. What is React?

Mention that React is a library for describing UI as a function of state and props. Components return elements, and React updates the DOM when inputs change. Clarify that React does not automatically provide routing, server state, authentication, or a backend.

### 2. What causes a component to re-render?

Its state changes, its parent renders with changed inputs, its props change, or a context value it consumes changes. A re-render recalculates output; it does not mean the entire DOM is rebuilt.

### 3. Why are keys important?

Keys provide stable identity for list items. They let React preserve the correct component state when items are inserted, removed, or reordered. Use domain IDs or `useFieldArray` IDs instead of indexes when identity can change.

### 4. What is the difference between state and props?

Props are read-only inputs from a parent; state is owned by a component and can change over time. A child requests a prop change through a callback rather than mutating the prop.

### 5. When should you use `useEffect`?

When synchronizing with an external system: network requests, browser storage, subscriptions, timers, document APIs, or third-party widgets. Do not use it for simple calculations or event actions that can happen directly in a handler.

### 6. Why can an effect run more than once in development?

Strict Mode intentionally replays certain setup behavior to expose unsafe side effects. Effects should have correct dependencies and cleanup and should tolerate setup/cleanup repetition.

### 7. Controlled versus uncontrolled input?

A controlled input receives its value from React state/form state and reports changes through an event. An uncontrolled input stores its value in the DOM and can be read through a ref or form submission. React Hook Form uses performant registration patterns and `Controller` for controlled third-party components.

### 8. Context versus props?

Props make data flow explicit and are best for local relationships. Context avoids passing a cross-cutting value through many intermediate components. Use it narrowly because every consumer can respond to provider value changes.

### 9. `useMemo` versus `useCallback`?

`useMemo` memoizes a calculated value; `useCallback` memoizes a function reference. Both are performance/identity tools, not defaults. Use them when measurement or a dependency contract justifies them.

### 10. How do you prevent state mutation?

Create new arrays and objects with spread, `map`, `filter`, or carefully designed helpers. Treat previous state as read-only so React identity comparisons remain reliable.

## TypeScript

### 11. Why use TypeScript in React?

It documents props, form values, API responses, and state; catches invalid field names and missing cases; improves refactoring and editor support. It does not validate runtime JSON, so external data still needs runtime validation.

### 12. `unknown` versus `any`?

`unknown` forces a runtime check before use; `any` turns off checking. Use `unknown` for parsed JSON, caught errors, storage, and external responses until narrowed.

### 13. Interface versus type?

Both describe shapes. Interfaces are extendable object contracts; type aliases compose unions, intersections, and primitives especially well. Consistency and accurate modeling matter more than a universal rule.

### 14. How do you type an API response?

Define the documented response shape and pass it to Axios generics, then validate untrusted runtime data when the server boundary is not trusted:

```tsx
const response = await api.get<WorkflowRunResponse>("/runs");
```

Do not use a type assertion to pretend arbitrary JSON is valid.

## APIs and backend calls

### 15. What happens after clicking a submit button?

Describe event dispatch, form validation, payload creation, Axios interceptors, cookies/headers, HTTP transport, backend auth/authz/validation/business logic, response status/body, promise resolution, state update, and re-render.

### 16. What is REST?

REST is an architectural style emphasizing resources, representations, stateless requests, standard HTTP semantics, and uniform interfaces. Mention that many real APIs are REST-like rather than perfectly RESTful.

### 17. What is the difference between 401 and 403?

401 means authentication is missing or invalid; 403 means the server understood the caller but refuses access. A frontend should redirect or refresh authentication for 401 according to the application flow, and show a permission message for 403.

### 18. What is CORS?

CORS is a browser-enforced permission mechanism for cross-origin requests. The backend must return appropriate headers for the frontend origin, especially when credentials are included. It is not fixed by adding a random frontend header.

### 19. Why use an Axios instance?

To centralize base URL, timeout, credentials, interceptors, error normalization, and cross-cutting request behavior. It prevents each component from implementing authentication and configuration differently.

### 20. How do you handle an API failure?

Classify the failure, preserve the error for diagnostics, show a user-appropriate message, keep the UI consistent, and offer a valid next action. Treat 401, 403, 422, 409, 429, and 5xx differently. Do not swallow errors or show success-shaped fallbacks.

### 21. How do you prevent duplicate submissions?

Track a pending state, disable the action, guard the handler, and make the backend operation idempotent or use an idempotency key where appropriate. UI protection alone is not sufficient for distributed systems.

### 22. How do you handle stale requests?

Cancel obsolete requests with `AbortController`, or associate each request with a sequence and ignore responses that are no longer current. Clean up on unmount and avoid setting UI state from an obsolete request.

### 23. What does `202 Accepted` mean?

The server accepted the request for processing but has not necessarily completed it. The UI should show queued/running status and use a documented status mechanism rather than displaying success prematurely.

## Security

### 24. Where should secrets live?

On a trusted backend or secret manager, never in frontend source, `VITE_` variables, local storage, or a shipped bundle. Browser configuration is public.

### 25. What is the difference between authentication and authorization?

Authentication identifies the caller; authorization decides whether that caller can perform an action. Both must be enforced on the backend.

### 26. How do cookies affect frontend requests?

The browser decides whether to attach cookies based on domain, path, Secure, SameSite, and request context. Axios `withCredentials` opts into credentialed cross-origin requests, but the server must also allow the origin and credentials.

### 27. How does React reduce XSS risk?

React escapes text interpolated into JSX by default. The developer can bypass that protection with unsafe HTML APIs, unsafe URL handling, or third-party code, so untrusted content must be sanitized and constrained.

## Architecture and practical judgment

### 28. How would you structure a growing React app?

Keep app bootstrap/providers separate from feature screens, shared UI primitives separate from business logic, and API/service boundaries explicit. Let features own feature state; use context for genuinely cross-cutting values; add routing and server-state tools when the problem justifies them.

### 29. How would you improve this project?

Give evidence-based ideas, for example:

- Add automated tests around critical workflows and API failure states.
- Extract repeated API calls and domain types into typed services.
- Add a consistent request-state abstraction where duplication is proven.
- Add route-level code splitting if feature size affects startup.
- Improve accessibility and error consistency.
- Introduce a server-state cache only after measuring duplicated fetching.

Do not criticize code merely because you prefer another style. Explain impact, migration risk, and validation.

### 30. How do you decide whether to extract a component?

Extract when a unit has a clear responsibility, is repeated, has independent state/behavior, or is difficult to test/read. Avoid abstractions that only save a few lines or hide important business behavior.

### 31. How do you test a React feature?

Test behavior from the user’s perspective: render, interact, submit, inspect visible results, and simulate API success/failure. Unit test pure validation and transformation functions. Add integration coverage for critical auth and request flows. Avoid tests that assert private implementation details.

### 32. How do you investigate a production bug?

Reproduce safely, identify the affected layer, inspect browser request/response data, correlate logs, check recent changes/configuration, form a hypothesis, make the smallest safe fix, add regression coverage, and document the cause and prevention.

---

# Part XXIII - Final revision sheets

## Before an interview

Be able to do all of the following without opening the code:

- Draw the `index.html -> main.tsx -> AuthProvider -> App -> feature` tree.
- Explain controlled inputs and `useFieldArray`.
- Explain why the Axios instance has `baseURL`, credentials, timeout, and interceptors.
- Distinguish 401, 403, 422, 429, and 500.
- Explain cookies, sessions, OAuth/OIDC, CORS, and CSRF.
- Explain how a stale request can corrupt UI and how to prevent it.
- Describe one real bug using the STAR format.
- Propose one improvement with a measured reason, not a preference.
- Explain what must never be put in a frontend bundle.

## One-page debugging sequence

```text
1. Reproduce.
2. Is the event handler running?
3. Did state/form values change?
4. Did a request appear?
5. Is URL/method/body/header correct?
6. What status and response body returned?
7. Is auth/authz involved?
8. Is the response stale or the UI state stale?
9. What is the smallest root-cause fix?
10. What regression test or check prevents recurrence?
```

## One-page implementation sequence

```text
1. Read the user story and API contract.
2. Define domain and API types.
3. Sketch the component tree.
4. Decide state ownership.
5. Build static UI and accessibility.
6. Add local interactions.
7. Add validation.
8. Add loading/error/empty/success states.
9. Connect the shared API service.
10. Handle auth, retries, cancellation, and duplicate actions.
11. Test happy and unhappy paths.
12. Run lint/build and review the diff.
```

## Completion standard

You are interview-ready for this project when you can:

1. Build a small React feature from an empty folder.
2. Read this application without getting lost in the component tree.
3. Implement a typed form and API call using existing conventions.
4. Explain the request at the HTTP, browser, Axios, backend, and UI layers.
5. Diagnose failures using evidence instead of guessing.
6. Discuss security and data boundaries accurately.
7. Explain trade-offs and limitations honestly.
8. Describe your work clearly in 30 seconds, 2 minutes, and 10 minutes.

The best preparation is not memorizing these pages. It is repeatedly building, breaking, debugging, and explaining the same concepts until you can transfer them to a new feature and a new interview question.

---

# Part XXIV - Copy-paste practice labs

The examples in this section are intentionally self-contained. They use public browser APIs or mocked data, so they do not require HERE network access, credentials, a private backend, or this repository.

## 24.1 Which online editor should you use?

Use one of these:

| Goal | Editor |
| --- | --- |
| HTML, CSS, and browser JavaScript | CodePen, JSFiddle, or PlayCode |
| React with JavaScript | CodeSandbox or StackBlitz React template |
| React with TypeScript | StackBlitz Vite React TypeScript template or CodeSandbox Vite TypeScript |
| Full local project | This repository with `npm run local` |
| Quick JavaScript expression | Browser DevTools Console |

For React examples:

1. Open a Vite React TypeScript template.
2. Replace `src/App.tsx` with the example.
3. If an example imports only from `react`, no additional package is needed.
4. If an example uses `lucide-react`, Axios, Zod, or React Hook Form, install the package in the editor or use the self-contained alternative provided.

For plain HTML examples, create an `index.html`, paste the complete example, and press Run. The examples do not require a build step.

## 24.2 How to use every lab

For every example:

1. Run the original version.
2. Predict what will happen before changing it.
3. Change one line.
4. Observe the output and browser console.
5. Explain the result in interview language.
6. Restore the example and complete the challenge.

Each lab has:

- **Concept:** the skill being practiced.
- **Runnable example:** code you can paste directly.
- **Expected result:** what you should see.
- **Interview answer:** how to explain it.
- **Challenge:** a change that proves you understand it.

---

## 24.3 HTML semantics and accessibility

**Concept:** Use elements according to meaning. A real button receives keyboard behavior and communicates its role to assistive technology.

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Accessible task form</title>
  </head>
  <body>
    <main>
      <h1>Tasks</h1>
      <form id="task-form">
        <label for="title">Task title</label>
        <input id="title" name="title" required />
        <button type="submit">Add task</button>
      </form>
      <p id="message" role="status" aria-live="polite"></p>
      <ul id="tasks"></ul>
    </main>

    <script>
      const form = document.querySelector("#task-form");
      const titleInput = document.querySelector("#title");
      const message = document.querySelector("#message");
      const tasks = document.querySelector("#tasks");

      form.addEventListener("submit", (event) => {
        event.preventDefault();
        const title = titleInput.value.trim();
        if (!title) return;

        const item = document.createElement("li");
        item.textContent = title;
        tasks.append(item);
        message.textContent = `Added "${title}"`;
        form.reset();
        titleInput.focus();
      });
    </script>
  </body>
</html>
```

**Expected result:** The form adds a task, announces the result, and returns focus to the input.

**Interview answer:** “Semantic HTML provides native behavior, accessibility semantics, and keyboard support. I use ARIA to enhance semantics, not to replace proper elements.”

**Challenge:** Add a delete button to every task. Ensure it is a real button and that its accessible name includes the task title.

## 24.4 CSS box model and layout

**Concept:** Every element is a box. `box-sizing: border-box` makes declared width include padding and border.

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <style>
      * { box-sizing: border-box; }
      body { margin: 0; font-family: system-ui, sans-serif; background: #eef2ff; }
      .dashboard {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 16px;
        max-width: 900px;
        margin: 40px auto;
        padding: 16px;
      }
      .card {
        min-width: 0;
        padding: 20px;
        border: 1px solid #c7d2fe;
        border-radius: 12px;
        background: white;
        box-shadow: 0 4px 14px rgb(15 23 42 / 10%);
      }
      @media (max-width: 700px) {
        .dashboard { grid-template-columns: 1fr; margin: 16px auto; }
      }
    </style>
  </head>
  <body>
    <main class="dashboard">
      <article class="card"><h2>Queued</h2><strong>12</strong></article>
      <article class="card"><h2>Running</h2><strong>3</strong></article>
      <article class="card"><h2>Failed</h2><strong>1</strong></article>
    </main>
  </body>
</html>
```

**Expected result:** Three cards appear in a row on a wide screen and stack on a narrow screen.

**Interview answer:** “I use Grid when I need two-dimensional layout, Flexbox for one-dimensional alignment, and responsive constraints instead of fixed widths.”

**Challenge:** Inspect the cards in DevTools and toggle padding, border, and `min-width` to observe the box model.

## 24.5 JavaScript values, functions, and arrays

**Concept:** React relies on ordinary JavaScript transformations such as `map`, `filter`, and immutable object updates.

Paste into the browser console:

```js
const runs = [
  { id: 1, name: "Stitch", status: "succeeded", duration: 12 },
  { id: 2, name: "Observable", status: "failed", duration: 4 },
  { id: 3, name: "Conflation", status: "running", duration: null },
];

const failedNames = runs
  .filter((run) => run.status === "failed")
  .map((run) => run.name);

const updatedRuns = runs.map((run) =>
  run.id === 3 ? { ...run, status: "succeeded", duration: 20 } : run
);

console.log(failedNames); // ["Observable"]
console.log(runs[2].status); // "running"
console.log(updatedRuns[2]); // changed copy
```

**Expected result:** The original `runs` array is unchanged; `updatedRuns` contains a new object for ID 3.

**Interview answer:** “I avoid mutating state because React relies heavily on reference identity. I create new arrays and objects when changing nested data.”

**Challenge:** Add a `totalDuration` using `reduce`, but ignore runs whose duration is `null`.

## 24.6 Closures and stale values

**Concept:** A closure retains access to variables from its surrounding scope. React event handlers also close over values from a render.

```js
function createCounter() {
  let count = 0;
  return {
    read: () => count,
    increment: () => { count += 1; },
  };
}

const counter = createCounter();
counter.increment();
counter.increment();
console.log(counter.read()); // 2
```

**Interview answer:** “A closure is a function plus the lexical environment it remembers. In React, closures explain why an event handler sees the state from the render in which it was created.”

**Challenge:** Write a function `makeMultiplier(factor)` that returns a function multiplying any supplied number.

## 24.7 Promises and `async/await`

**Concept:** Async code represents work that completes later and can succeed or fail.

```js
const wait = (milliseconds) =>
  new Promise((resolve) => setTimeout(resolve, milliseconds));

async function loadMessage() {
  console.log("loading");
  try {
    await wait(500);
    console.log("success");
    return "Data loaded";
  } catch (error) {
    console.error("failed", error);
    throw error;
  } finally {
    console.log("finished");
  }
}

loadMessage().then(console.log);
```

**Expected result:** `loading`, then `success`, `finished`, and `Data loaded`.

**Interview answer:** “`async` functions return promises. `await` pauses that function until the promise settles, while `try/catch/finally` expresses failure and cleanup.”

**Challenge:** Change `wait` to reject and verify that the error branch runs and the rejection is visible.

## 24.8 TypeScript types and narrowing

Paste into a TypeScript React editor or TypeScript Playground:

```ts
type RequestState<T> =
  | { status: "idle" }
  | { status: "loading" }
  | { status: "success"; data: T }
  | { status: "error"; message: string };

function describeRequest(state: RequestState<string[]>) {
  switch (state.status) {
    case "idle":
      return "Not started";
    case "loading":
      return "Loading...";
    case "success":
      return `${state.data.length} items`;
    case "error":
      return `Error: ${state.message}`;
  }
}

console.log(describeRequest({ status: "success", data: ["A", "B"] }));
```

**Expected result:** `2 items`, with compile-time protection against reading `data` from an error state.

**Interview answer:** “A discriminated union represents mutually exclusive states. Checking the discriminant narrows the type and prevents impossible property access.”

**Challenge:** Add a `"cancelled"` state and let TypeScript show every place that needs updating.

## 24.9 Minimal React component and props

Paste into `src/App.tsx` in a React TypeScript editor:

```tsx
type BadgeProps = {
  label: string;
  tone?: "neutral" | "success" | "danger";
};

function Badge({ label, tone = "neutral" }: BadgeProps) {
  const colors = {
    neutral: "#475569",
    success: "#15803d",
    danger: "#b91c1c",
  };

  return (
    <span style={{ color: "white", background: colors[tone], padding: 6 }}>
      {label}
    </span>
  );
}

export default function App() {
  return (
    <main style={{ display: "grid", gap: 12 }}>
      <h1>Workflow status</h1>
      <Badge label="Succeeded" tone="success" />
      <Badge label="Failed" tone="danger" />
    </main>
  );
}
```

**Expected result:** The parent supplies props and the child renders each badge.

**Interview answer:** “Props are immutable inputs. The parent owns the data and the child focuses on rendering the contract it receives.”

**Challenge:** Add an optional `onClick` callback and make the badge a real button only when the callback exists.

## 24.10 State and immutable updates in React

```tsx
import { useState } from "react";

type Task = { id: number; title: string; done: boolean };

export default function App() {
  const [tasks, setTasks] = useState<Task[]>([
    { id: 1, title: "Understand state", done: false },
  ]);

  const toggle = (id: number) => {
    setTasks((current) =>
      current.map((task) =>
        task.id === id ? { ...task, done: !task.done } : task
      )
    );
  };

  return (
    <main>
      <h1>{tasks.filter((task) => !task.done).length} open task(s)</h1>
      {tasks.map((task) => (
        <label key={task.id} style={{ display: "block" }}>
          <input
            type="checkbox"
            checked={task.done}
            onChange={() => toggle(task.id)}
          />
          {task.title}
        </label>
      ))}
    </main>
  );
}
```

**Expected result:** Checking the task updates both the checkbox and count.

**Interview answer:** “The setter receives an updater because the next state depends on the previous state. `map` returns a new collection and spreads only the changed item.”

**Challenge:** Add an input and append a task without mutating the existing array.

## 24.11 Controlled input and form submission

```tsx
import { useState } from "react";

export default function App() {
  const [name, setName] = useState("");
  const [submitted, setSubmitted] = useState("");

  const submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const value = name.trim();
    if (!value) return;
    setSubmitted(value);
    setName("");
  };

  return (
    <main>
      <form onSubmit={submit}>
        <label htmlFor="name">Your name</label>
        <input
          id="name"
          value={name}
          onChange={(event) => setName(event.target.value)}
        />
        <button type="submit">Submit</button>
      </form>
      {submitted && <p>Hello, {submitted}</p>}
    </main>
  );
}
```

**Expected result:** The page prevents navigation, validates whitespace, and displays the submitted name.

**Interview answer:** “A controlled input has one source of truth in React. `preventDefault` stops the browser’s full-page form navigation so the component can handle submission.”

**Challenge:** Add an inline error instead of silently returning for an empty value.

## 24.12 Conditional rendering and list keys

```tsx
type User = { id: string; name: string; online: boolean };

const users: User[] = [
  { id: "u1", name: "Asha", online: true },
  { id: "u2", name: "Noah", online: false },
];

export default function App() {
  return (
    <main>
      {users.length === 0 ? (
        <p>No users found.</p>
      ) : (
        <ul>
          {users.map((user) => (
            <li key={user.id}>
              {user.name} {user.online ? "Online" : "Offline"}
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
```

**Expected result:** Each user gets stable identity and an appropriate status.

**Interview answer:** “Keys are not merely warnings; they tell React which conceptual item is which across renders. Stable IDs preserve the right local state.”

**Challenge:** Replace `key={user.id}` with `key={index}`, reorder the users, and explain why indexes can create bugs in editable lists.

## 24.13 `useEffect` and cleanup

```tsx
import { useEffect, useState } from "react";

export default function App() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setSeconds((value) => value + 1);
    }, 1000);

    return () => window.clearInterval(timer);
  }, []);

  return <p>Elapsed: {seconds}s</p>;
}
```

**Expected result:** The counter increments once per second and stops when the component unmounts.

**Interview answer:** “The effect synchronizes a timer outside React. Cleanup prevents the timer from continuing after unmount and avoids leaks or updates to a removed component.”

**Challenge:** Add a `document.title` update in a separate effect with `seconds` as a dependency.

## 24.14 `useRef` without re-rendering

```tsx
import { useRef, useState } from "react";

export default function App() {
  const inputRef = useRef<HTMLInputElement>(null);
  const renderCount = useRef(0);
  const [, forceRender] = useState(0);
  renderCount.current += 1;

  return (
    <main>
      <input ref={inputRef} placeholder="Focus me" />
      <button type="button" onClick={() => inputRef.current?.focus()}>
        Focus input
      </button>
      <button type="button" onClick={() => forceRender((value) => value + 1)}>
        Render again
      </button>
      <p>Render count: {renderCount.current}</p>
    </main>
  );
}
```

**Expected result:** The ref focuses the input. Updating the ref itself would not cause a render; the second button does.

**Interview answer:** “A ref persists across renders and is useful for DOM access or mutable bookkeeping. It is not a substitute for state when the value belongs in the UI.”

**Challenge:** Store the last button-click timestamp in a ref and display it only after a separate render.

## 24.15 Context and a custom hook

```tsx
import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from "react";

type ThemeContextValue = {
  dark: boolean;
  toggle: () => void;
};

const ThemeContext = createContext<ThemeContextValue | null>(null);

function ThemeProvider({ children }: { children: ReactNode }) {
  const [dark, setDark] = useState(false);
  const value = { dark, toggle: () => setDark((current) => !current) };
  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

function useTheme() {
  const value = useContext(ThemeContext);
  if (!value) throw new Error("useTheme must be inside ThemeProvider");
  return value;
}

function Toolbar() {
  const { dark, toggle } = useTheme();
  return (
    <button type="button" onClick={toggle}>
      Theme: {dark ? "dark" : "light"}
    </button>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <Toolbar />
    </ThemeProvider>
  );
}
```

**Expected result:** `Toolbar` reads theme data without receiving it as a prop.

**Interview answer:** “Context is appropriate for cross-cutting values such as theme or session. The custom hook validates provider usage. I would not put rapidly changing form fields into a broad context.”

**Challenge:** Persist the theme in `localStorage` and synchronize a `dark` class on `document.documentElement`.

## 24.16 Fetch with loading, success, and error

This example uses a public endpoint commonly available for practice. If it is blocked by an editor, replace it with a local mock promise.

```tsx
import { useEffect, useState } from "react";

type Todo = { id: number; title: string; completed: boolean };

export default function App() {
  const [state, setState] = useState<
    | { status: "loading" }
    | { status: "success"; todos: Todo[] }
    | { status: "error"; message: string }
  >({ status: "loading" });

  useEffect(() => {
    const controller = new AbortController();

    fetch("https://jsonplaceholder.typicode.com/todos?_limit=5", {
      signal: controller.signal,
    })
      .then((response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json() as Promise<Todo[]>;
      })
      .then((todos) => setState({ status: "success", todos }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === "AbortError") return;
        setState({
          status: "error",
          message: error instanceof Error ? error.message : "Request failed",
        });
      });

    return () => controller.abort();
  }, []);

  if (state.status === "loading") return <p>Loading...</p>;
  if (state.status === "error") return <p role="alert">{state.message}</p>;

  return (
    <ul>
      {state.todos.map((todo) => (
        <li key={todo.id}>{todo.title}</li>
      ))}
    </ul>
  );
}
```

**Expected result:** Loading appears first, then five todos or an error message.

**Interview answer:** “I model request state explicitly, check `response.ok`, narrow caught errors, and abort obsolete requests during cleanup. A TypeScript type describes the expected shape but does not replace runtime validation.”

**Challenge:** Add a refresh button and ensure two requests cannot render stale data over a newer request.

## 24.17 Mocking a backend locally

Use this when an online editor cannot access a public endpoint:

```tsx
type Run = { id: string; status: "running" | "succeeded" | "failed" };

function mockGetRuns(): Promise<Run[]> {
  return new Promise((resolve) => {
    window.setTimeout(
      () =>
        resolve([
          { id: "run-1", status: "succeeded" },
          { id: "run-2", status: "failed" },
        ]),
      600
    );
  });
}

async function loadRuns() {
  const runs = await mockGetRuns();
  console.log(runs);
}

void loadRuns();
```

The UI should not care whether its data came from a mock promise or Axios. Keep the boundary typed and replace the implementation later.

**Interview answer:** “Mocking isolates UI behavior from backend availability. I keep the mock’s contract compatible with the real API so replacing it does not require rewriting the component.”

## 24.18 Axios request shape without private configuration

If Axios is installed in the editor:

```tsx
import axios from "axios";

type User = { id: number; name: string };

async function loadUser() {
  const response = await axios.get<User>(
    "https://jsonplaceholder.typicode.com/users/1",
    { timeout: 5000 }
  );
  console.log(response.data.name);
}

void loadUser().catch((error: unknown) => {
  if (axios.isAxiosError(error)) {
    console.error("HTTP failure", error.response?.status);
  } else {
    console.error("Unexpected failure", error);
  }
});
```

For this repository, replace direct Axios usage with:

```tsx
import api from "./services/setupAxios";

const response = await api.get<User>("/users/1");
```

**Interview answer:** “A shared Axios instance centralizes base URL, credentials, timeouts, and interceptors. Components should not recreate that cross-cutting configuration.”

## 24.19 Form validation without a package

This is executable in any React TypeScript editor:

```tsx
import { useState } from "react";

type Errors = { name?: string; email?: string };

export default function App() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [errors, setErrors] = useState<Errors>({});
  const [message, setMessage] = useState("");

  const submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors: Errors = {};
    if (!name.trim()) nextErrors.name = "Name is required";
    if (!email.includes("@")) nextErrors.email = "Enter a valid email";
    setErrors(nextErrors);
    setMessage(Object.keys(nextErrors).length ? "" : "Valid form");
  };

  return (
    <form onSubmit={submit} noValidate>
      <label htmlFor="name">Name</label>
      <input id="name" value={name} onChange={(e) => setName(e.target.value)} />
      {errors.name && <p role="alert">{errors.name}</p>}
      <label htmlFor="email">Email</label>
      <input id="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      {errors.email && <p role="alert">{errors.email}</p>}
      <button type="submit">Validate</button>
      <p role="status">{message}</p>
    </form>
  );
}
```

**Interview answer:** “Client validation gives fast feedback, but server validation remains mandatory because the client can be bypassed.”

**Challenge:** Install Zod, express the same rules in a schema, and compare field-level errors.

## 24.20 Dynamic fields

This package-free example demonstrates the underlying React idea behind `useFieldArray`:

```tsx
import { useState } from "react";

type Row = { id: string; value: string };

export default function App() {
  const [rows, setRows] = useState<Row[]>([
    { id: crypto.randomUUID(), value: "" },
  ]);

  const update = (id: string, value: string) => {
    setRows((current) =>
      current.map((row) => (row.id === id ? { ...row, value } : row))
    );
  };

  return (
    <main>
      {rows.map((row) => (
        <div key={row.id}>
          <input
            value={row.value}
            onChange={(event) => update(row.id, event.target.value)}
          />
          <button
            type="button"
            onClick={() =>
              setRows((current) => current.filter((item) => item.id !== row.id))
            }
          >
            Remove
          </button>
        </div>
      ))}
      <button
        type="button"
        onClick={() =>
          setRows((current) => [
            ...current,
            { id: crypto.randomUUID(), value: "" },
          ])
        }
      >
        Add row
      </button>
    </main>
  );
}
```

**Interview answer:** “Dynamic rows need stable identity. The array index is useful for a data path, but a stable ID should be the React key so row state does not move to another row.”

## 24.21 Error boundaries: what they catch and what they do not

```tsx
import { Component, type ErrorInfo, type ReactNode } from "react";

type Props = { children: ReactNode };
type State = { hasError: boolean };

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("Render failure", error, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return <p role="alert">This section could not be displayed.</p>;
    }
    return this.props.children;
  }
}
```

An error boundary catches errors thrown during rendering, lifecycle methods, and constructors below it. It does not automatically catch errors in event handlers, async callbacks, or server requests; those need their own `try/catch`.

**Interview answer:** “Error boundaries protect rendering failures and provide a fallback. They complement, rather than replace, request error handling and event-handler error handling.”

## 24.22 Race-condition demonstration

```tsx
import { useEffect, useState } from "react";

function fakeSearch(query: string) {
  const delay = query === "slow" ? 1000 : 100;
  return new Promise<string[]>((resolve) => {
    window.setTimeout(() => resolve([`Result for ${query}`]), delay);
  });
}

export default function App() {
  const [query, setQuery] = useState("slow");
  const [results, setResults] = useState<string[]>([]);

  useEffect(() => {
    let current = true;
    void fakeSearch(query).then((value) => {
      if (current) setResults(value);
    });
    return () => {
      current = false;
    };
  }, [query]);

  return (
    <main>
      <button type="button" onClick={() => setQuery("slow")}>Slow</button>
      <button type="button" onClick={() => setQuery("fast")}>Fast</button>
      <p>Query: {query}</p>
      <p>{results.join(", ")}</p>
    </main>
  );
}
```

**Expected result:** An obsolete slow response cannot overwrite a newer fast result.

**Interview answer:** “The cleanup marks the previous request obsolete. In production I prefer cancellation when supported, or a request ID/sequence check, and I handle aborts separately from real failures.”

---

# Part XXV - Interview coding drills

Solve these without looking at the solution first. Use an online React TypeScript editor and explain your choices aloud.

## Drill 1: Counter with constraints

Build a counter with increment, decrement, reset, and a maximum of 10. Disable increment at 10 and decrement at 0.

**Interviewer is testing:** state updates, functional setters, derived disabled state, button semantics, and boundary conditions.

**Follow-up:** What changes if two increments happen before React renders?

## Drill 2: Searchable list

Render 20 users, add a text search, show an empty state, and highlight the count.

**Interviewer is testing:** controlled inputs, `filter`, derived data, case normalization, stable keys, and empty states.

**Follow-up:** How would you debounce a real API search?

## Drill 3: Debounced search

```tsx
import { useEffect, useState } from "react";

export default function App() {
  const [query, setQuery] = useState("");
  const [debounced, setDebounced] = useState("");

  useEffect(() => {
    const timer = window.setTimeout(() => setDebounced(query), 300);
    return () => window.clearTimeout(timer);
  }, [query]);

  return (
    <main>
      <input
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder="Search"
      />
      <p>Searching for: {debounced || "nothing"}</p>
    </main>
  );
}
```

**Follow-up:** What would you cancel when a real request starts?

## Drill 4: Request-state component

Build a reusable component that accepts `status` as a discriminated union and renders idle, loading, success, or error.

**Interviewer is testing:** TypeScript narrowing and state modeling.

## Drill 5: Modal dialog

Build a modal with open/close, Escape handling, background click handling, and focus returned to the trigger.

**Interviewer is testing:** effects, refs, cleanup, keyboard accessibility, and composition. In production, prefer the existing Radix dialog primitive instead of reimplementing difficult accessibility behavior.

## Drill 6: Pagination

Create a paginated list with `page`, `pageSize`, previous/next controls, disabled boundaries, and loading state.

**Follow-up:** Why might cursor pagination be safer than page numbers on changing data?

## Drill 7: Retryable API action

Create a “Run workflow” button with pending state, success/error feedback, and protection against double clicks.

**Follow-up:** What backend guarantee would you want before automatically retrying?

## Drill 8: Normalize API data

Given an API response with nullable fields, convert it into a UI model with safe defaults. Explain why the transformation belongs at the API boundary.

## Drill 9: Review broken code

Explain the bugs:

```tsx
function BrokenList({ items, setItems }) {
  const [filter, setFilter] = useState("");

  useEffect(() => {
    setItems(items.filter((item) => item.name.includes(filter)));
  }, [items, filter, setItems]);

  return items.map((item, index) => (
    <input key={index} value={item.name} />
  ));
}
```

Expected observations:

- It stores derived filtered data as source state.
- It may create an update loop or overwrite the source collection.
- Inputs are read-only because they have no `onChange`.
- Index keys can break identity when filtering/reordering.
- `setItems` ownership is unclear.

## Drill 10: Explain a 401

Write the exact steps you would take when a request returns 401 in this project. A strong answer mentions Network inspection, session endpoint, cookies, Axios response interceptor, auth redirect, environment mode, and backend logs.

---

# Part XXVI - Interview answer bank: short, medium, and deep

For each topic, practice three answer lengths.

## React rendering

**Short:** “React recalculates component output when state, props, or consumed context changes and commits the necessary DOM updates.”

**Medium:** “A component render should be pure. React creates an element description, compares it with the previous one, and commits only required DOM changes. Keys preserve identity in lists.”

**Deep:** Discuss render versus commit, state queues, batching, Strict Mode, reconciliation, stable identity, and why side effects belong outside render.

## `useEffect`

**Short:** “It synchronizes React with an external system.”

**Medium:** “I use it for network requests, subscriptions, timers, browser APIs, or storage. Dependencies describe what the synchronization reads, and cleanup reverses setup.”

**Deep:** Discuss stale closures, abort controllers, dependency identity, Strict Mode replay, race conditions, and when an effect is unnecessary.

## State management

**Short:** “I keep state at the lowest common owner and use Context for narrow cross-cutting values.”

**Medium:** “Local UI state stays local, derived state is calculated, complex transitions may use a reducer, and server state should have explicit fetching/cache behavior.”

**Deep:** Discuss server state versus client state, normalized data, cache invalidation, provider re-render scope, and when a state library is justified.

## API error handling

**Short:** “I classify the status, preserve diagnostics, show actionable UI, and keep state consistent.”

**Medium:** “401, 403, validation errors, conflicts, rate limits, and transient server failures need different user actions. I avoid swallowing errors and do not retry non-idempotent work blindly.”

**Deep:** Discuss error envelopes, correlation IDs, retries/backoff, idempotency keys, cancellation, partial failures, observability, and contract ownership.

## Security

**Short:** “The browser is public; secrets and authorization belong on trusted servers.”

**Medium:** “I rely on backend authorization, secure cookie settings or an approved token flow, input validation at the server boundary, safe rendering, and explicit CORS/CSRF protections.”

**Deep:** Discuss threat models, XSS, CSRF, SameSite, HttpOnly, Secure, OAuth/OIDC roles, least privilege, logging hygiene, dependency risk, and defense in depth.

---

# Part XXVII - Final online-practice schedule

Use this schedule if you want a repeatable self-study plan. It is a checklist, not a requirement to finish in a particular number of days.

| Session | Practice | Evidence to keep |
| --- | --- | --- |
| 1 | HTML semantics and accessible form | Working HTML link |
| 2 | CSS box model, Flexbox, Grid, responsive layout | Screenshot at two widths |
| 3 | JavaScript arrays, objects, functions, closures | Console output |
| 4 | Promises, `fetch`, status codes, DevTools Network | Request screenshot with secrets removed |
| 5 | JSX, components, props | React editor link |
| 6 | State, events, immutable list updates | Working task board |
| 7 | Controlled forms and validation | Error and success screenshots |
| 8 | Effects, cleanup, refs, context | Explanation recorded aloud |
| 9 | TypeScript unions, generics, narrowing | Compiler errors resolved |
| 10 | API mock, loading/error/empty/success | Working request-state screen |
| 11 | Axios, cancellation, retry, pagination | Design notes |
| 12 | Read this repository end to end | Architecture diagram |
| 13 | Implement the capstone | Code and validation output |
| 14 | Complete interview drills | Recorded answers |

For each session, explain:

```text
What did I build?
What data changes?
What can fail?
How does the user know?
How would I test it?
How would I explain it to an interviewer?
```

## Final note on interview readiness

No guide can honestly promise that you will clear every interview. Interviewers evaluate problem solving, communication, fundamentals, coding, design judgment, and experience differently. This handbook gives you the strongest repeatable preparation: run every lab, modify it, debug it, explain it, and connect it back to the real project. That is how the knowledge becomes usable under interview pressure.
