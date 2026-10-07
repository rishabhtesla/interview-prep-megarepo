import { afterEach, expect, it, vi } from "vitest";
import { request } from "./api.js";

afterEach(() => vi.useRealTimers());

it("makes a non-JSON proxy failure visible", async () => {
  vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
    ok: false, status: 502, json: async () => { throw new SyntaxError(); },
  }));
  await expect(request("/api/topics")).rejects.toThrow("HTTP 502");
});

it("bounds a stalled request and describes ambiguous save outcomes", async () => {
  vi.useFakeTimers();
  vi.stubGlobal("fetch", vi.fn((path, options) => new Promise((resolve, reject) => {
    options.signal.addEventListener("abort", () => reject(new DOMException("Aborted", "AbortError")));
  })));
  const result = expect(request("/api/progress/react-effects", { method: "PUT" }))
    .rejects.toThrow("Reload to check whether a save reached the server");
  await vi.advanceTimersByTimeAsync(6000);
  await result;
});
