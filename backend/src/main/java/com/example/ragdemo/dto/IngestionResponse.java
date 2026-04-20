package com.example.ragdemo.dto;

/**
 * Response payload returned after document ingestion.
 *
 * @param documentTitle Name of the ingested document.
 * @param chunksCreated Number of vectorized chunks created from the document.
 * @param totalChunks   Current total chunks in the vector store.
 */
public record IngestionResponse(String documentTitle, int chunksCreated, int totalChunks) {
}
