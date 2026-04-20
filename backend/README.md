# Spring Boot RAG + Vector DB Demo (Ollama)

This backend demonstrates **Retrieval-Augmented Generation (RAG)** using:
- Spring Boot REST APIs
- Local Ollama for embeddings and LLM generation
- An in-memory vector database implementation (cosine similarity nearest-neighbor search)

## How RAG Works In This Project

1. **Ingestion**
   - A document is posted to `/api/rag/ingest`.
   - The text is split into chunks.
   - Each chunk is converted to an embedding via Ollama (`/api/embeddings`).
   - Chunks + embeddings are stored in the vector store.

2. **Question Answering**
   - A question is posted to `/api/rag/ask`.
   - The question is embedded with the same embedding model.
   - Vector similarity search returns top-K closest chunks.
   - Retrieved chunks are inserted into a grounded prompt.
   - Ollama generation endpoint (`/api/generate`) produces the final answer.

## How Vector DB Works Here

The `InMemoryVectorStore` acts as a lightweight vector DB:
- Stores vectors for each chunk in memory.
- Computes cosine similarity between query vector and chunk vectors.
- Sorts by similarity score and returns top-K nearest chunks.

In production, replace this with external vector DBs like pgvector, Milvus, Pinecone, or Weaviate.

## Class-by-Class Documentation

### `com.example.ragdemo.RagDemoApplication`
Bootstraps the Spring Boot app and component scanning.

### `com.example.ragdemo.config.CorsConfig`
Enables browser CORS requests from the React dashboard (`http://localhost:5173`) to backend APIs.

### `com.example.ragdemo.controller.RagController`
API layer with endpoints:
- `POST /api/rag/ingest`
- `POST /api/rag/ask`
- `GET /api/rag/status`

Delegates all business logic to `RagService`.

### `com.example.ragdemo.dto.DocumentIngestRequest`
Input payload for ingestion; validates title/content fields.

### `com.example.ragdemo.dto.IngestionResponse`
Ingestion result details: created chunk count and total indexed chunks.

### `com.example.ragdemo.dto.AskRequest`
Input payload for questions and optional `topK`.

### `com.example.ragdemo.dto.RetrievedChunkView`
Response view model for showing retrieved chunks in the dashboard (metadata + score + text).

### `com.example.ragdemo.dto.AskResponse`
Final RAG response containing:
- generated answer
- prompt used for generation
- retrieved chunk list

### `com.example.ragdemo.model.VectorChunk`
Domain model for one vectorized chunk with text, metadata, and embedding vector.

### `com.example.ragdemo.model.ScoredChunk`
Simple wrapper combining a `VectorChunk` with similarity score.

### `com.example.ragdemo.repository.InMemoryVectorStore`
Implements vector DB semantics:
- save chunks
- count chunks
- top-K nearest-neighbor search by cosine similarity

### `com.example.ragdemo.service.OllamaClient`
Handles all Ollama HTTP calls:
- `embed(text)` -> embedding vector
- `generate(prompt)` -> model answer text

### `com.example.ragdemo.service.RagService`
Core RAG orchestration:
- chunking strategy
- ingestion flow (chunk -> embed -> store)
- query flow (embed question -> retrieve -> prompt build -> generate)

### `com.example.ragdemo.service.SampleDataLoader`
Seeds sample RAG/vector knowledge at startup so dashboard works immediately.

## API Examples

### Ingest
```bash
curl -X POST http://localhost:8080/api/rag/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Company Policy",
    "source": "hr-handbook",
    "content": "Employees can work remotely up to 3 days per week..."
  }'
```

### Ask
```bash
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "How many days remote work is allowed?",
    "topK": 3
  }'
```

## Run Backend

1. Ensure Ollama is running:
   ```bash
   ollama serve
   ```
2. Pull models:
   ```bash
   ollama pull llama3.2
   ollama pull nomic-embed-text
   ```
3. Start backend:
   ```bash
   mvn spring-boot:run
   ```

Backend URL: `http://localhost:8080`
