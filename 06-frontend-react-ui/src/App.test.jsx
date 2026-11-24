import { StrictMode } from "react";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import App from "./App.jsx";
import AlgorithmLab from "./AlgorithmLab.jsx";
import GeoJsonMutationLab from "./GeoJsonMutationLab.jsx";

const topics = [
  { id: "react-effects", track: "react", title: "Render and effects", summary: "Clean up a request", minutes: 60 },
  { id: "spring-transactions", track: "spring", title: "Transactions", summary: "Proxy boundaries", minutes: 75 },
];
const json = (body, status = 200) => ({ ok: status < 400, status, json: async () => body });

function api({ progress = [], writeError = false } = {}) {
  const mock = vi.fn(async (path, options) => {
    if (options?.method === "PUT") {
      if (writeError) return json({ detail: "Catalog unavailable; progress was not saved." }, 503);
      return json({ topicId: "react-effects", ...JSON.parse(options.body) });
    }
    return json(path === "/api/topics" ? topics : progress);
  });
  vi.stubGlobal("fetch", mock);
  return mock;
}

describe("study planner", () => {
  it("steps through GeoJSON mutation stages", async () => {
    const user = userEvent.setup();
    render(<GeoJsonMutationLab />);
    expect(screen.getByRole("heading", { name: "Raw partner geometry" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Next stage" }));
    expect(screen.getByRole("heading", { name: "Normalized geometry" })).toBeInTheDocument();
    expect(screen.getByText(/"status":"normalized"/)).toBeInTheDocument();
  });

  it("steps through the binary-search visualization", async () => {
    const user = userEvent.setup();
    render(<AlgorithmLab />);
    expect(screen.getByText(/Step 1 of/)).toBeInTheDocument();
    await user.clear(screen.getByLabelText("Binary search target"));
    await user.type(screen.getByLabelText("Binary search target"), "3");
    await user.click(screen.getByRole("button", { name: "Next step" }));
    expect(screen.getByText(/Step 2 of/)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Next step" }));
    expect(screen.getByText(/Found 3 at index 0/)).toBeInTheDocument();
  });

  it("shows loading, restores saved state, filters and preserves hidden drafts", async () => {
    api({ progress: [{ topicId: "spring-transactions", completed: true, note: "Done" }] });
    const user = userEvent.setup();
    render(<StrictMode><App /></StrictMode>);
    expect(screen.getByText(/Loading catalog/)).toBeInTheDocument();
    expect(await screen.findByText("1 of 2 topics completed (saved)")).toBeInTheDocument();
    await user.type(screen.getByLabelText("Study note for Render and effects"), "Keep this draft");
    await user.selectOptions(screen.getByLabelText("Track"), "spring");
    expect(screen.queryByRole("heading", { name: "Render and effects" })).not.toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText("Track"), "react");
    expect(screen.getByLabelText("Study note for Render and effects")).toHaveValue("Keep this draft");
    await user.type(screen.getByLabelText("Search topics"), "nonexistent");
    expect(screen.getByText(/No topics match/)).toBeInTheDocument();
  });

  it("persists explicit state, not a toggle, and updates saved count only on success", async () => {
    const fetch = api();
    const user = userEvent.setup();
    render(<App />);
    const form = await screen.findByRole("form", { name: "Progress for Render and effects" });
    await user.click(within(form).getByRole("checkbox"));
    await user.type(within(form).getByRole("textbox"), "Explain cleanup");
    expect(screen.getByText("0 of 2 topics completed (saved)")).toBeInTheDocument();
    await user.click(within(form).getByRole("button", { name: "Save progress" }));
    expect(await within(form).findByText("Saved to progress-service.")).toBeInTheDocument();
    expect(screen.getByText("1 of 2 topics completed (saved)")).toBeInTheDocument();
    const write = fetch.mock.calls.find(([, options]) => options.method === "PUT");
    expect(write[0]).toBe("/api/progress/react-effects");
    expect(JSON.parse(write[1].body)).toEqual({ completed: true, note: "Explain cleanup" });
    expect(within(form).getByRole("button")).toBeDisabled();
  });

  it("keeps unsaved edits and announces write failures without changing saved count", async () => {
    api({ writeError: true });
    const user = userEvent.setup();
    render(<App />);
    const form = await screen.findByRole("form", { name: "Progress for Render and effects" });
    await user.click(within(form).getByRole("checkbox"));
    await user.type(within(form).getByRole("textbox"), "Retry later");
    await user.click(within(form).getByRole("button"));
    expect(await within(form).findByRole("alert")).toHaveTextContent("HTTP 503");
    expect(within(form).getByRole("textbox")).toHaveValue("Retry later");
    expect(screen.getByText("0 of 2 topics completed (saved)")).toBeInTheDocument();
    expect(within(form).getByRole("button")).toBeEnabled();
  });

  it("reports initial failure and retries both reads", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("offline")));
    const user = userEvent.setup();
    render(<App />);
    expect(await screen.findByRole("alert")).toHaveTextContent("Cannot reach the API");
    api();
    await user.click(screen.getByRole("button", { name: "Retry loading" }));
    expect(await screen.findByText("0 of 2 topics completed (saved)")).toBeInTheDocument();
  });

  it("aborts reads on unmount instead of accepting stale results", async () => {
    const signals = [];
    vi.stubGlobal("fetch", vi.fn((path, options) => {
      signals.push(options.signal);
      return new Promise((resolve, reject) => {
        options.signal.addEventListener("abort", () => reject(new DOMException("Aborted", "AbortError")));
      });
    }));
    const { unmount } = render(<App />);
    unmount();
    await waitFor(() => expect(signals.every((signal) => signal.aborted)).toBe(true));
    expect(signals).toHaveLength(2);
  });
});
