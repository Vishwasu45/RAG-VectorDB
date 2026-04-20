package com.example.ragdemo.model;

/**
 * Wrapper object containing a chunk plus its similarity score against a query embedding.
 *
 * @param chunk Chunk metadata and text.
 * @param score Cosine similarity score.
 */
public record ScoredChunk(VectorChunk chunk, double score) {
}
