import { useCallback, useEffect, useState } from "react";
import { loadStudy, saveProgress } from "./api.js";

export function useStudy() {
  const [attempt, setAttempt] = useState(0);
  const [state, setState] = useState({ status: "loading" });
  useEffect(() => {
    const controller = new AbortController();
    setState({ status: "loading" });
    loadStudy(controller.signal).then(
      (data) => {
        if (!controller.signal.aborted) setState({ status: "ready", ...data });
      },
      (error) => {
        if (!controller.signal.aborted) setState({ status: "error", error: error.message });
      },
    );
    return () => controller.abort();
  }, [attempt]);

  const save = useCallback(async (topicId, changes) => {
    const result = await saveProgress(topicId, changes);
    setState((current) => ({
      ...current,
      progress: { ...current.progress, [topicId]: result },
    }));
    return result;
  }, []);

  return { ...state, save, retry: () => setAttempt((value) => value + 1) };
}
