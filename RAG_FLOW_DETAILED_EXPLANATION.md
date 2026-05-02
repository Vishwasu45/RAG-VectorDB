# Detailed RAG Flow Explanation (Frontend -> Backend -> Ollama)

This document explains how this project works end-to-end:
- how data flows from the React frontend to the Spring Boot backend
- how documents are ingested, chunked, embedded, and stored
- how user questions are embedded and matched with relevant chunks
- how the final answer is generated using retrieved context

---

## 1) High-Level Architecture

This repository has two applications:

- `frontend` (React + Vite): user-facing dashboard
- `backend` (Spring Boot): RAG pipeline API

External dependency:

- **Ollama** running locally (`http://localhost:11434`) for:
  - embedding generation (`nomic-embed-text`)
  - final answer generation (`llama3.2`)

The backend exposes REST endpoints under:

- `http://localhost:8080/api/rag`

The frontend calls these endpoints:

- `GET /status`
- `POST /ingest`
- `POST /ask`

---

## 2) Frontend -> Backend Request Flow

### Frontend API layer

In `frontend/src/api.js`, all calls are centralized:

- Base URL: `http://localhost:8080/api/rag`
- `getStatus()` -> `GET /status`
- `ingestDocument(payload)` -> `POST /ingest`
- `askQuestion(payload)` -> `POST /ask`

The helper function:
- adds `Content-Type: application/json`
- throws a readable error when the response is not OK
- returns parsed JSON on success

### Frontend UI flow

`frontend/src/App.jsx` wires everything:

1. On load, `useEffect` calls `refreshStatus()` -> backend `/status`.
2. When user submits ingestion form (`IngestPanel`):
   - payload `{ title, source, content }`
   - sent via `ingestDocument(...)`
   - success message and status refresh are shown
3. When user submits question form (`QueryPanel`):
   - payload `{ question, topK }`
   - sent via `askQuestion(...)`
   - response is rendered by `PipelineView`:
     - final answer
     - retrieved chunks with score/source/chunk index
     - exact prompt sent to LLM

### Backend entry point

`RagController` (`backend/.../controller/RagController.java`) receives the requests:

- `/ingest` validates `DocumentIngestRequest` and calls `ragService.ingestDocument(...)`
- `/ask` validates `AskRequest` and calls `ragService.askQuestion(...)`
- `/status` returns indexed chunk count and a pipeline string

So the controller is thin; orchestration logic is in `RagService`.

---

## 3) How Document Ingestion Works (Chunking + Vectorization)

Document ingestion happens in:

- `RagService.ingestDocument(String title, String source, String content)`

Step-by-step:

1. **Create document ID**
   - A UUID is generated for the document.

2. **Chunk raw text**
   - `chunkContent(content, MAX_CHUNK_SIZE)` with `MAX_CHUNK_SIZE = 500`.
   - Normalization:
     - remove `\r`
     - trim
   - Split strategy:
     - if double-newline blocks exist: split by paragraph (`\n\n+`)
     - else: split by sentence boundary regex `(?<=\.)\s+`
   - Build chunk strings up to max 500 chars.
   - If one unit is larger than 500 chars, hard-split into fixed-size segments.

3. **Embed each chunk**
   - For each chunk, call `ollamaClient.embed(chunkText)`.
   - This calls Ollama `POST /api/embeddings` with:
     - `"model": "nomic-embed-text"`
     - `"prompt": <chunk text>`
   - Response field `"embedding"` is converted to `List<Double>`.

4. **Create vector records**
   - Each chunk becomes a `VectorChunk` with:
     - `chunkId` (UUID)
     - `documentId`
     - `title`
     - `source` (defaults to `"manual-input"` if blank)
     - `chunkIndex`
     - `text`
     - `embedding`

5. **Store vectors**
   - `vectorStore.saveAll(vectorChunks)` persists chunks into `InMemoryVectorStore`.
   - Storage is in-memory (`CopyOnWriteArrayList`), so it is not durable across app restarts.

6. **Return ingestion response**
   - `IngestionResponse(documentTitle, chunksCreated, totalChunks)`

Important behavior:
- If normalized content is blank, chunk list becomes empty.
- In that case, zero chunks are stored, and response reflects that.

---

## 4) How Vector Search / Retrieval Works

Vector retrieval runs in:

- `RagService.askQuestion(...)` -> `vectorStore.search(...)`
- similarity math in `InMemoryVectorStore.cosineSimilarity(...)`

Process:

1. **Determine topK**
   - If request `topK` is null or <= 0, fallback to default `3`.
   - During search, at least one result is requested (`Math.max(1, topK)`), though empty store still returns none.

2. **Embed user question**
   - Call `ollamaClient.embed(question)` with the same embedding model.
   - This keeps query/document vectors in same semantic space.

3. **Score all chunks**
   - For each stored `VectorChunk`, compute cosine similarity:
     - `dot(a,b) / (||a|| * ||b||)`
   - If vectors are null, empty, or dimension mismatch, score is `0.0`.

4. **Sort and keep top-K**
   - Scores sorted descending.
   - Highest similarity chunks are returned as `List<ScoredChunk>`.

5. **Prepare API-safe retrieval view**
   - Each scored result is transformed to `RetrievedChunkView` for frontend display:
     - chunk metadata
     - similarity score
     - text preview (currently full chunk text)

---

## 5) How Questions Are Answered Using Retrieved Chunks

Question-answering happens in `RagService.askQuestion(...)`:

1. **Build context block**
   - Retrieved chunks are converted into text like:
     - `[<title>] <chunk text>`
   - Joined with blank lines into one context string.

2. **Build grounded prompt**
   - Prompt template:
     - instruct assistant to answer using provided context
     - if answer is missing from context, clearly say so
     - include `Context`, `Question`, then `Answer:`

3. **Generate answer with LLM**
   - `ollamaClient.generate(prompt)` calls Ollama `POST /api/generate`:
     - model: `llama3.2`
     - stream: `false`
   - Reads `"response"` from Ollama JSON and trims whitespace.

4. **Return full pipeline result**
   - `AskResponse(question, answer, prompt, retrievedDocs)`
   - Frontend renders all 4 fields in `PipelineView`.

This design is useful for debugging because users can inspect:
- what was retrieved
- how strongly it matched (score)
- exact prompt sent to model
- final generated answer

---

## 6) End-to-End Example Sequence

### A) Ingest flow example

1. User types title/source/content in `IngestPanel`.
2. Frontend sends `POST /api/rag/ingest`.
3. Controller validates payload and delegates to `RagService`.
4. Service chunks text, embeds each chunk with Ollama embeddings API.
5. Chunks + vectors saved in in-memory store.
6. Backend returns chunk counts.
7. Frontend refreshes `/status` and shows updated indexed chunks.

### B) Ask flow example

1. User asks question + topK in `QueryPanel`.
2. Frontend sends `POST /api/rag/ask`.
3. Service embeds question.
4. Service retrieves topK similar chunks from vector store via cosine similarity.
5. Service builds a context-grounded prompt.
6. Service calls Ollama generation API for final answer.
7. Backend returns answer + retrieved chunks + prompt.
8. Frontend displays everything in `PipelineView`.

---

## 7) Startup Seed Data Behavior

On backend startup, `SampleDataLoader` (`CommandLineRunner`) automatically ingests two seed documents:

- `RAG Basics`
- `Vector Database Concepts`

That means the system has immediately queryable chunks even before manual ingestion.

---

## 8) Configuration and Models

From `backend/src/main/resources/application.yml`:

- server port: `8080`
- Ollama base URL: `http://localhost:11434`
- chat model: `llama3.2`
- embedding model: `nomic-embed-text`

Operational implication:
- Ollama server must be running locally.
- Required models must be pulled (`ollama pull ...`) before usage.

---

## 9) Current Design Constraints (Important for Review)

1. **In-memory vector store only**
   - all vectors are lost on backend restart
   - suitable for demo, not production persistence

2. **Brute-force similarity search**
   - compares query against every stored chunk
   - fine for small datasets, not scalable for very large corpora

3. **Chunking is character-based heuristic**
   - simple and readable, but not token-aware
   - no overlap between chunks

4. **No metadata filtering in retrieval**
   - retrieval is purely semantic similarity across all stored chunks

5. **Prompt includes raw chunk text**
   - transparent for learning/debugging
   - may need truncation/formatting controls at larger scales

---

## 10) One-Line Mental Model

**Ingest path:** text -> chunk -> embed -> store vectors  
**Query path:** question -> embed -> cosine topK retrieval -> grounded prompt -> LLM answer

