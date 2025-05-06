package com.company.bootcamp.task5.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.bootcamp.task5.model.QdrantVectorRS;
import com.company.bootcamp.task5.services.EmbeddingService;
import com.microsoft.semantickernel.services.textembedding.Embedding;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/build")
    public Mono<List<Embedding>> buildEmbedding(@RequestParam String text){
        return embeddingService.build(text);
    }

    @PutMapping("/store")
    public Mono<ResponseEntity<String>> storeEmbedding(@RequestBody List<Map<String, Object>> input) {
        return embeddingService.store(input)
                .thenReturn(ResponseEntity.ok("Saved successfully."));
    }

    @GetMapping("/search")
    public Mono<List<QdrantVectorRS>> searchClosestEmbeddings(@RequestParam String text) {
        return embeddingService.search(text);
    }
}