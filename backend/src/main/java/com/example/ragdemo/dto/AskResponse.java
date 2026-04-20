package com.example.ragdemo.dto;

import java.util.List;

/**
 * Response payload from a RAG question.
 *
 * @param question      Original user question.
 * @param answer        Final generated answer from Ollama.
 * @param prompt        Prompt sent to the generation model.
 * @param retrievedDocs Retrieved chunks used as grounding context.
 */
public record AskResponse(
        String question,
        String answer,
        String prompt,
        List<RetrievedChunkView> retrievedDocs
) {
}
