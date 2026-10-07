import { useState } from "react";

export default function TopicCard({ topic, progress, save, hidden }) {
  const [completed, setCompleted] = useState(progress?.completed ?? false);
  const [note, setNote] = useState(progress?.note ?? "");
  const [status, setStatus] = useState("idle");
  const [message, setMessage] = useState("");
  const dirty = completed !== (progress?.completed ?? false) || note !== (progress?.note ?? "");
  const busy = status === "saving";

  async function submit(event) {
    event.preventDefault();
    if (busy) return;
    setStatus("saving");
    setMessage("");
    try {
      await save(topic.id, { completed, note });
      setStatus("saved");
      setMessage("Saved to progress-service.");
    } catch (error) {
      setStatus("error");
      setMessage(`${error.message} Your edits remain here; they are not confirmed saved.`);
    }
  }

  return <li hidden={hidden}>
    <article aria-labelledby={`${topic.id}-title`}>
      <p className="eyebrow">{topic.track} · {topic.minutes} minutes</p>
      <h2 id={`${topic.id}-title`}>{topic.title}</h2>
      <p>{topic.summary}</p>
      <form onSubmit={submit} aria-label={`Progress for ${topic.title}`} aria-busy={busy}>
        <fieldset disabled={busy}>
          <legend className="sr-only">Study progress</legend>
          <label className="check">
            <input type="checkbox" checked={completed} onChange={(event) => {
              setCompleted(event.target.checked);
              setMessage("");
            }} />
            Completed: {topic.title}
          </label>
          <label htmlFor={`${topic.id}-note`}>Study note for {topic.title}</label>
          <textarea id={`${topic.id}-note`} value={note} maxLength={500} rows={3}
            aria-describedby={`${topic.id}-limit`}
            onChange={(event) => { setNote(event.target.value); setMessage(""); }} />
          <small id={`${topic.id}-limit`}>{note.length}/500 characters. No sensitive information.</small>
          <button type="submit" disabled={!dirty || busy}>
            {busy ? "Saving…" : "Save progress"}
          </button>
        </fieldset>
        {dirty && !busy && <p className="unsaved">Unsaved edits</p>}
        <p role={status === "error" ? "alert" : "status"}>{message}</p>
      </form>
    </article>
  </li>;
}
