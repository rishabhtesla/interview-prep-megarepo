export async function request(path, { signal, ...options } = {}) {
  const controller = new AbortController();
  const abort = () => controller.abort();
  signal?.addEventListener("abort", abort, { once: true });
  if (signal?.aborted) controller.abort();
  let timedOut = false;
  const timeout = setTimeout(() => {
    timedOut = true;
    controller.abort();
  }, 6000);
  try {
    const response = await fetch(path, {
      ...options,
      headers: { "Content-Type": "application/json", ...options.headers },
      signal: controller.signal,
    });
    if (!response.ok) {
      const problem = await response.json().catch(() => null);
      throw new Error(`HTTP ${response.status}: ${problem?.detail || "Request failed. Check that both services are running."}`);
    }
    return await response.json();
  } catch (error) {
    if (timedOut) throw new Error("Request timed out. Reload to check whether a save reached the server.");
    if (controller.signal.aborted) throw error;
    if (error instanceof TypeError) throw new Error("Cannot reach the API. Check the services and retry.");
    throw error;
  } finally {
    clearTimeout(timeout);
    signal?.removeEventListener("abort", abort);
  }
}

export function loadStudy(signal) {
  return Promise.all([
    request("/api/topics", { signal }),
    request("/api/progress", { signal }),
  ]).then(([topics, progress]) => {
    if (!Array.isArray(topics) || !Array.isArray(progress)) {
      throw new Error("The API returned an unexpected response.");
    }
    return { topics, progress: Object.fromEntries(progress.map((item) => [item.topicId, item])) };
  });
}

export function saveProgress(topicId, changes) {
  return request(`/api/progress/${encodeURIComponent(topicId)}`, {
    method: "PUT",
    body: JSON.stringify(changes),
  });
}
