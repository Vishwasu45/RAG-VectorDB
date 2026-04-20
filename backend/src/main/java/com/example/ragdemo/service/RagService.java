package com.example.ragdemo.service;

import com.example.ragdemo.dto.AskResponse;
import com.example.ragdemo.dto.IngestionResponse;
import com.example.ragdemo.dto.RetrievedChunkView;
import com.example.ragdemo.model.ScoredChunk;
import com.example.ragdemo.model.VectorChunk;
import com.example.ragdemo.repository.InMemoryVectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main orchestration service for the RAG pipeline.
 * <p>
 * Responsibilities:
 * - Ingestion path: chunk text, generate embeddings, save vectors.
 * - Query path: embed user question, retrieve nearest chunks, build grounded prompt, generate answer.
 */
@Service
public class RagService {

    private static final int DEFAULT_TOP_K = 3;
    private static final int MAX_CHUNK_SIZE = 500;

    private final InMemoryVectorStore vectorStore;
    private final OllamaClient ollamaClient;

    public RagService(InMemoryVectorStore vectorStore, OllamaClient ollamaClient) {
        this.vectorStore = vectorStore;
        this.ollamaClient = ollamaClient;
    }

    /**
     * Ingests a document into the vector store by chunking and embedding it.
     */
    public IngestionResponse ingestDocument(String title, String source, String content) {
        String documentId = UUID.randomUUID().toString();
        List<String> chunks = chunkContent(content, MAX_CHUNK_SIZE);

        List<VectorChunk> vectorChunks = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            List<Double> embedding = ollamaClient.embed(chunkText);
            vectorChunks.add(new VectorChunk(
                    UUID.randomUUID().toString(),
                    documentId,
                    title,
                    source == null || source.isBlank() ? "manual-input" : source,
                    i,
                    chunkText,
                    embedding
            ));
        }

        vectorStore.saveAll(vectorChunks);
        return new IngestionResponse(title, vectorChunks.size(), vectorStore.count());
    }

    /**
     * Answers a question using retrieval-augmented generation.
     */
    public AskResponse askQuestion(String question, Integer topK) {
        int effectiveTopK = (topK == null || topK <= 0) ? DEFAULT_TOP_K : topK;
        List<Double> questionEmbedding = ollamaClient.embed(question);
        List<ScoredChunk> retrieved = vectorStore.search(questionEmbedding, effectiveTopK);

        String context = retrieved.stream()
                .map(scoredChunk -> "[" + scoredChunk.chunk().title() + "] " + scoredChunk.chunk().text())
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                You are an assistant that must answer using the provided context.
                If the answer is not in the context, clearly say so.

                Context:
                %s

                Question:
                %s

                Answer:
                """.formatted(context, question);

        String answer = ollamaClient.generate(prompt).trim();
        List<RetrievedChunkView> retrievedViews = retrieved.stream()
                .map(sc -> new RetrievedChunkView(
                        sc.chunk().chunkId(),
                        sc.chunk().documentId(),
                        sc.chunk().title(),
                        sc.chunk().source(),
                        sc.chunk().chunkIndex(),
                        sc.score(),
                        sc.chunk().text()
                ))
                .toList();

        return new AskResponse(question, answer, prompt, retrievedViews);
    }

    /**
     * @return number of chunks currently indexed in the vector store.
     */
    public int totalChunks() {
        return vectorStore.count();
    }

    private List<String> chunkContent(String content, int maxChunkSize) {
        String normalized = content.replace("\r", "").trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        List<String> paragraphs = normalized.split("\n\n+").length > 1
                ? List.of(normalized.split("\n\n+"))
                : List.of(normalized.split("(?<=\\.)\\s+"));

        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String part : paragraphs) {
            String candidate = part.trim();
            if (candidate.isEmpty()) {
                continue;
            }

            if (current.length() + candidate.length() + 1 > maxChunkSize && current.length() > 0) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }

            if (candidate.length() > maxChunkSize) {
                int start = 0;
                while (start < candidate.length()) {
                    int end = Math.min(start + maxChunkSize, candidate.length());
                    chunks.add(candidate.substring(start, end).trim());
                    start = end;
                }
                continue;
            }

            current.append(candidate).append("\n");
        }

        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}
