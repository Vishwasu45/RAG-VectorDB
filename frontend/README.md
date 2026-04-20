# React Dashboard for RAG Demo

This dashboard visualizes the RAG pipeline:
- Ingest document text
- Run question answering
- Inspect retrieved chunks, similarity scores, and final prompt

## Dashboard Features

- **Ingest Panel**: sends text to backend for embedding + vector indexing.
- **Ask Panel**: submits user question and top-K retrieval parameter.
- **Pipeline Output**:
  - generated answer
  - retrieved chunks with score/source/chunk index
  - exact prompt sent to LLM

## Run Frontend

```bash
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

The backend must run at `http://localhost:8080`.
