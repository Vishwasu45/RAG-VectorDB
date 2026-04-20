package com.example.ragdemo.repository;

import com.example.ragdemo.model.ScoredChunk;
import com.example.ragdemo.model.VectorChunk;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal in-memory vector database implementation for demo purposes.
 * <p>
 * This repository stores chunk embeddings and supports nearest-neighbor retrieval using cosine similarity.
 * In production, replace it with a dedicated vector database like pgvector, Milvus, Weaviate, or Pinecone.
 */
@Repository
public class InMemoryVectorStore {

    private final List<VectorChunk> chunks = new CopyOnWriteArrayList<>();

    /**
     * Inserts a batch of chunks into the vector store.
     */
    public void saveAll(List<VectorChunk> newChunks) {
        chunks.addAll(newChunks);
    }

    /**
     * @return current count of indexed chunks.
     */
    public int count() {
        return chunks.size();
    }

    /**
     * Finds top K most similar chunks by cosine similarity.
     */
    public List<ScoredChunk> search(List<Double> queryEmbedding, int topK) {
        if (chunks.isEmpty()) {
            return List.of();
        }

        List<ScoredChunk> scored = new ArrayList<>(chunks.size());
        for (VectorChunk chunk : chunks) {
            double score = cosineSimilarity(queryEmbedding, chunk.embedding());
            scored.add(new ScoredChunk(chunk, score));
        }

        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return scored.stream().limit(Math.max(1, topK)).toList();
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty() || a.size() != b.size()) {
            return 0.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double x = a.get(i);
            double y = b.get(i);
            dot += x * y;
            normA += x * x;
            normB += y * y;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
