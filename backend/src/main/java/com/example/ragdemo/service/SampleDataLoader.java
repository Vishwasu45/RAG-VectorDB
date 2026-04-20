package com.example.ragdemo.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds sample knowledge on startup so users can query the dashboard immediately.
 */
@Component
public class SampleDataLoader implements CommandLineRunner {

    private final RagService ragService;

    public SampleDataLoader(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public void run(String... args) {
        ragService.ingestDocument(
                "RAG Basics",
                "seed",
                """
                Retrieval-Augmented Generation (RAG) combines information retrieval with language generation.
                First, documents are converted to embeddings and stored in a vector database.
                At query time, the user question is embedded and the nearest chunks are retrieved by similarity.
                The retrieved chunks are injected into the prompt so the model can answer with grounded facts.
                """
        );

        ragService.ingestDocument(
                "Vector Database Concepts",
                "seed",
                """
                A vector database stores dense vectors produced by embedding models.
                Each vector represents the semantic meaning of text.
                Similarity metrics such as cosine similarity are used to perform nearest-neighbor search.
                This allows semantic retrieval even when exact keywords are not present.
                """
        );
    }
}
