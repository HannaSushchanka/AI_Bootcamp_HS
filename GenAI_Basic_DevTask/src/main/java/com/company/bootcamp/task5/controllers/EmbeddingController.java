package com.company.bootcamp.task5.controllers;


import com.azure.ai.openai.models.Embeddings;
import com.company.bootcamp.task5.services.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    @Autowired
    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/build")
    public Map<String, Object> buildEmbedding(@RequestParam String text) {
        Embeddings embedding = embeddingService.generateEmbedding(text);
        return Map.of("text", text, "embedding", embedding);
    }

    @PostMapping("/store")
    public Map<String, Object> buildAndStoreEmbedding(@RequestParam String text, @RequestParam String id) {
        Embeddings embedding = embeddingService.generateEmbedding(text);
        embedding = embeddingService.storeEmbedding(embedding,text);
        return Map.of("id", id, "text", text, "embedding", embedding);
    }

    @GetMapping("/search")
    public Map<String, Object> searchClosestEmbeddings(@RequestParam String text, @RequestParam int topK) {
        Embeddings embedding = embeddingService.generateEmbedding(text);
        List<String> closestIds = embeddingService.searchClosestEmbeddings(embedding.getData().getFirst().getEmbedding(), topK);
        return Map.of("text", text, "closest_ids", closestIds);
    }
}
