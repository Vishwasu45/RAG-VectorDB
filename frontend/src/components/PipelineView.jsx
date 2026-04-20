export default function PipelineView({ result }) {
  if (!result) {
    return (
      <section className="panel">
        <h2>Pipeline Output</h2>
        <p className="muted">Run a question to view retrieved chunks, prompt, and generated answer.</p>
      </section>
    );
  }

  return (
    <section className="panel">
      <h2>Pipeline Output</h2>
      <div className="block">
        <h3>Answer</h3>
        <p>{result.answer}</p>
      </div>

      <div className="block">
        <h3>Retrieved Chunks</h3>
        {result.retrievedDocs.length === 0 && <p className="muted">No chunks matched.</p>}
        {result.retrievedDocs.map((chunk) => (
          <article key={chunk.chunkId} className="chunk">
            <header>
              <strong>{chunk.title}</strong>
              <span>score: {chunk.score.toFixed(4)}</span>
            </header>
            <p className="muted">
              source: {chunk.source} | chunk #{chunk.chunkIndex}
            </p>
            <p>{chunk.textPreview}</p>
          </article>
        ))}
      </div>

      <div className="block">
        <h3>Prompt Sent To LLM</h3>
        <pre>{result.prompt}</pre>
      </div>
    </section>
  );
}
