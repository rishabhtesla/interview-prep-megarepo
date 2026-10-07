import { useState } from "react";
import { useStudy } from "./useStudy.js";
import TopicCard from "./TopicCard.jsx";

export default function App() {
  const study = useStudy();
  const [track, setTrack] = useState("all");
  const [query, setQuery] = useState("");
  const [onlyIncomplete, setOnlyIncomplete] = useState(false);
  const topics = study.topics ?? [];
  const tracks = [...new Set(topics.map((topic) => topic.track))].sort();
  const normalizedQuery = query.trim().toLowerCase();
  const matches = (topic) => (track === "all" || topic.track === track)
    && `${topic.title} ${topic.summary}`.toLowerCase().includes(normalizedQuery)
    && (!onlyIncomplete || !study.progress?.[topic.id]?.completed);
  const visibleCount = topics.filter(matches).length;
  const completedCount = topics.filter((topic) => study.progress?.[topic.id]?.completed).length;

  return <>
    <a className="skip-link" href="#topics">Skip to topics</a>
    <main>
      <header>
        <p className="eyebrow">Six-track interview course</p>
        <h1>Interview Study Planner</h1>
        <p>Practice, explain your reasoning, then save your progress.</p>
        <p className="notice">Local learning demo: one shared learner, no login. Notes are stored on this machine, not in your browser.</p>
      </header>
      {study.status === "loading" && <p role="status">Loading catalog and saved progress…</p>}
      {study.status === "error" && <section aria-label="Connection error">
        <p role="alert">{study.error}</p>
        <button onClick={study.retry}>Retry loading</button>
      </section>}
      {study.status === "ready" && <>
        <section aria-label="Study overview">
          <p role="status">{completedCount} of {topics.length} topics completed (saved)</p>
          <progress aria-label="Saved topic completion" max={Math.max(topics.length, 1)} value={completedCount} />
        </section>
        <section className="filters" aria-label="Filter topics">
          <label>Track
            <select value={track} onChange={(event) => setTrack(event.target.value)}>
              <option value="all">All tracks</option>
              {tracks.map((name) => <option key={name} value={name}>{name}</option>)}
            </select>
          </label>
          <label>Search topics
            <input type="search" value={query} onChange={(event) => setQuery(event.target.value)} />
          </label>
          <label className="check"><input type="checkbox" checked={onlyIncomplete}
            onChange={(event) => setOnlyIncomplete(event.target.checked)} />Only incomplete</label>
        </section>
        <section id="topics" tabIndex={-1} aria-label="Study topics">
          <p aria-live="polite">{visibleCount} topics shown</p>
          {visibleCount === 0 && <p>No topics match. Clear your filters or start the catalog service with its seed data.</p>}
          <ul className="topics">
            {topics.map((topic) => <TopicCard key={topic.id} topic={topic}
              progress={study.progress[topic.id]} save={study.save} hidden={!matches(topic)} />)}
          </ul>
        </section>
      </>}
      <footer>Course chapters and worked solutions live in the six sibling track folders. Drafts are lost on reload; saved progress survives service restarts.</footer>
    </main>
  </>;
}
