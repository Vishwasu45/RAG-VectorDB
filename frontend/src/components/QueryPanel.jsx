import { useState } from "react";

export default function QueryPanel({ onAsk }) {
  const [question, setQuestion] = useState("");
  const [topK, setTopK] = useState(3);

  async function handleSubmit(event) {
    event.preventDefault();
    await onAsk({ question, topK: Number(topK) });
  }

  return (
    <section className="panel">
      <h2>Ask Question</h2>
      <p className="muted">Question is embedded, matched against vectors, then answered with retrieved context.</p>
      <form onSubmit={handleSubmit}>
        <textarea
          placeholder="Ask something about the ingested knowledge..."
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          rows={4}
          required
        />
        <label className="inline-label">
          Top-K chunks
          <input
            type="number"
            min={1}
            max={10}
            value={topK}
            onChange={(e) => setTopK(e.target.value)}
          />
        </label>
        <button type="submit">Run RAG</button>
      </form>
    </section>
  );
}
