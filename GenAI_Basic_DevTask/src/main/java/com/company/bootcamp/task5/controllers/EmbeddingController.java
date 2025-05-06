package com.company.bootcamp.task5.controllers;

import com.company.bootcamp.task5.model.QdrantVectorRS;
import com.company.bootcamp.task5.services.EmbeddingService;
import com.microsoft.semantickernel.services.textembedding.Embedding;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/build")
    public List<Embedding> buildEmbedding(@RequestParam String text) throws ExecutionException, InterruptedException {
        return embeddingService.build(text);
    }

    @PutMapping("/store")
    public ResponseEntity<String> storeEmbedding(@RequestBody List<Map<String, Object>> input) {
            embeddingService.store(input);
        return ResponseEntity.ok("Saved successfully.");
    }

    @GetMapping("/search")
    public List<QdrantVectorRS> searchClosestEmbeddings(@RequestParam String text) {
        return embeddingService.search(text);
    }
}
