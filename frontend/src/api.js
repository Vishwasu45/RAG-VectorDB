const BASE_URL = "http://localhost:8080/api/rag";

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with status ${response.status}`);
  }

  return response.json();
}

export function getStatus() {
  return request("/status");
}

export function ingestDocument(payload) {
  return request("/ingest", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function askQuestion(payload) {
  return request("/ask", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
