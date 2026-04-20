package com.example.ragdemo.dto;

/**
 * Represents a retrieved chunk shown in the dashboard and returned with RAG answers.
 *
 * @param chunkId     Unique chunk identifier.
 * @param documentId  Parent document identifier.
 * @param title       Parent document title.
 * @param source      Parent source label.
 * @param chunkIndex  Position of chunk in the document.
 * @param score       Cosine similarity score against the question.
 * @param textPreview Chunk text content.
 */
public record RetrievedChunkView(
        String chunkId,
        String documentId,
        String title,
        String source,
        int chunkIndex,
        double score,
        String textPreview
) {
}
