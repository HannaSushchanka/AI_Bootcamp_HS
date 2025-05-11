package com.company.bootcamp.task6.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.company.bootcamp.task6.clients.OpenAIClient;
import com.company.bootcamp.task6.clients.GRPSQdrantClient;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class RAGService {
    private OpenAIClient openAIClient;
    private GRPSQdrantClient qdrantClient;
    private EmbeddingService embeddingService;

    public Mono<String> getGeneratedAnswer(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Query must not be null or empty"));
        }

        return embeddingService.build(query)
                .flatMap(embeddings -> {
                    if (embeddings == null || embeddings.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Failed to generate embedding for the query"));
                    }

                    List<Double> queryEmbedding = embeddings.get(0).getVector()
                            .stream()
                            .map(Float::doubleValue)
                            .collect(Collectors.toList());

                    if (queryEmbedding.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Embedding vector is null or empty"));
                    }

                    return qdrantClient.searchRelevantChunks(queryEmbedding)
                            .collectList()
                            .flatMap(context -> {
                                if (context == null || context.isEmpty()) {
                                    return Mono.just("No relevant context found for the query.");
                                }

                                String prompt = "Answer the question using context:\n" +
                                        String.join("\n", context) +
                                        "\nQ: " + query;

                                return openAIClient.getCompletion(prompt);
                            });
                });
    }
}
