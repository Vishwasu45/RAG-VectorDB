import { useState } from "react";

export default function IngestPanel({ onIngest }) {
  const [title, setTitle] = useState("");
  const [source, setSource] = useState("");
  const [content, setContent] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    await onIngest({ title, source, content });
    setTitle("");
    setSource("");
    setContent("");
  }

  return (
    <section className="panel">
      <h2>Ingest Document</h2>
      <p className="muted">Add text knowledge so it is embedded and indexed in the vector store.</p>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Document title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
        <input
          type="text"
          placeholder="Source (optional)"
          value={source}
          onChange={(e) => setSource(e.target.value)}
        />
        <textarea
          placeholder="Paste document content"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={8}
          required
        />
        <button type="submit">Embed + Store</button>
      </form>
    </section>
  );
}
