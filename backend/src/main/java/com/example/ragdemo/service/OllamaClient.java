package com.example.ragdemo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Thin client for Ollama HTTP APIs used by this demo.
 * <p>
 * Supported operations:
 * - Generate embeddings for retrieval indexing and similarity search.
 * - Generate final natural-language answers from grounded prompts.
 */
@Component
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String chatModel;
    private final String embeddingModel;

    public OllamaClient(
            RestTemplateBuilder builder,
            @Value("${app.ollama.base-url}") String baseUrl,
            @Value("${app.ollama.chat-model}") String chatModel,
            @Value("${app.ollama.embedding-model}") String embeddingModel
    ) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(120))
                .build();
        this.baseUrl = baseUrl;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Converts text into a dense embedding vector using Ollama's embedding endpoint.
     */
    @SuppressWarnings("unchecked")
    public List<Double> embed(String text) {
        String url = baseUrl + "/api/embeddings";
        Map<String, Object> body = Map.of(
                "model", embeddingModel,
                "prompt", text
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
        if (response == null || !response.containsKey("embedding")) {
            throw new IllegalStateException("Ollama did not return embedding data");
        }

        return ((List<Number>) response.get("embedding"))
                .stream()
                .map(Number::doubleValue)
                .toList();
    }

    /**
     * Generates text from the chat/generation model using a single prompt.
     */
    public String generate(String prompt) {
        String url = baseUrl + "/api/generate";
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "prompt", prompt,
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
        if (response == null || !response.containsKey("response")) {
            throw new IllegalStateException("Ollama did not return generated text");
        }
        return String.valueOf(response.get("response"));
    }
}
