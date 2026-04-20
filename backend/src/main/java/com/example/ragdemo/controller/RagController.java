package com.example.ragdemo.controller;

import com.example.ragdemo.dto.AskRequest;
import com.example.ragdemo.dto.AskResponse;
import com.example.ragdemo.dto.DocumentIngestRequest;
import com.example.ragdemo.dto.IngestionResponse;
import com.example.ragdemo.service.RagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST API layer exposing operations for ingestion, retrieval, and dashboard status.
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Ingests a document into the vector store.
     */
    @PostMapping("/ingest")
    public IngestionResponse ingest(@Valid @RequestBody DocumentIngestRequest request) {
        return ragService.ingestDocument(request.title(), request.source(), request.content());
    }

    /**
     * Executes the RAG query pipeline for a question.
     */
    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return ragService.askQuestion(request.question(), request.topK());
    }

    /**
     * Returns lightweight metrics for the dashboard header.
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "indexedChunks", ragService.totalChunks(),
                "pipeline", "embed -> vector-search -> prompt-assembly -> generation"
        );
    }
}
