package com.example.ragdemo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to ingest a new knowledge document into the vector store.
 *
 * @param title   Human-friendly title for the document.
 * @param source  Optional source marker, such as a file name or URL.
 * @param content Full text content that will be chunked and embedded.
 */
public record DocumentIngestRequest(
        @NotBlank(message = "title is required") String title,
        String source,
        @NotBlank(message = "content is required") String content
) {
}
