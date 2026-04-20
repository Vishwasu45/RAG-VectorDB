package com.example.ragdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the RAG demo backend.
 * <p>
 * This application exposes APIs to ingest documents, store their embeddings in a vector store,
 * and answer user questions through a Retrieval-Augmented Generation (RAG) pipeline backed by Ollama.
 */
@SpringBootApplication
public class RagDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagDemoApplication.class, args);
    }
}
