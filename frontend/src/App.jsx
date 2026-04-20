import { useEffect, useState } from "react";
import { askQuestion, getStatus, ingestDocument } from "./api";
import IngestPanel from "./components/IngestPanel";
import QueryPanel from "./components/QueryPanel";
import PipelineView from "./components/PipelineView";

export default function App() {
  const [status, setStatus] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function refreshStatus() {
    const data = await getStatus();
    setStatus(data);
  }

  useEffect(() => {
    refreshStatus().catch((err) => setError(err.message));
  }, []);

  async function handleIngest(payload) {
    setLoading(true);
    setError("");
    try {
      const response = await ingestDocument(payload);
      setMessage(`Ingested "${response.documentTitle}" with ${response.chunksCreated} chunks.`);
      await refreshStatus();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleAsk(payload) {
    setLoading(true);
    setError("");
    try {
      const response = await askQuestion(payload);
      setResult(response);
      setMessage("RAG query completed.");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="container">
      <header>
        <h1>Spring Boot + Ollama RAG Dashboard</h1>
        <p className="muted">
          Flow: {status?.pipeline ?? "loading..."} | Indexed chunks: {status?.indexedChunks ?? "-"}
        </p>
        {loading && <p>Working...</p>}
        {message && <p className="success">{message}</p>}
        {error && <p className="error">{error}</p>}
      </header>

      <div className="grid">
        <IngestPanel onIngest={handleIngest} />
        <QueryPanel onAsk={handleAsk} />
      </div>

      <PipelineView result={result} />
    </main>
  );
}
