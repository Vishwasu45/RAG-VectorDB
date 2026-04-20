package com.example.ragdemo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for asking a question through the RAG pipeline.
 *
 * @param question Natural language user question.
 * @param topK     Number of nearest chunks to retrieve.
 */
public record AskRequest(
        @NotBlank(message = "question is required") String question,
        Integer topK
) {
}
