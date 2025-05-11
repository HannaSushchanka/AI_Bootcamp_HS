package com.company.bootcamp.task6.controllers;

import com.company.bootcamp.task6.model.QdrantVectorRQ;
import com.company.bootcamp.task6.services.QdrantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/qdrant")
public class QdrantController {
    private final QdrantService qdrantService;

    public QdrantController(QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    @PostMapping("/collection/create/{name}")
    public Mono<ResponseEntity<String>> createCollection(@PathVariable String name) {
        return qdrantService.createCollection(name)
                .map(response -> response.getResult()
                        ? ResponseEntity.status(HttpStatus.CREATED).body("Collection created successfully: " + name)
                        : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to create collection: " + name))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error creating collection: " + e.getMessage())));
    }

    @DeleteMapping("/collection/delete/{name}")
    public Mono<ResponseEntity<String>> deleteCollection(@PathVariable String name) {
        return qdrantService.deleteCollection(name)
                .map(response -> response.getResult()
                        ? ResponseEntity.ok("Collection deleted successfully: " + name)
                        : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to delete collection: " + name))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error deleting collection: " + e.getMessage())));
    }

    @PostMapping("/{collectionName}/vector/add")
    public Mono<ResponseEntity<String>> addVector(@PathVariable String collectionName, @RequestBody QdrantVectorRQ request) {
        return qdrantService.addVector(collectionName, request)
                .map(result -> ResponseEntity.ok("Vector inserted with ID: " + request.getId()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to insert vector: " + e.getMessage())));
    }

    @GetMapping("/{collectionName}/vector/{id}")
    public Mono<ResponseEntity<?>> getVector(@PathVariable String collectionName, @PathVariable UUID id) {
        return qdrantService.getVector(collectionName, id)
                .flatMap(response -> response.isEmpty()
                        ? Mono.just(ResponseEntity.notFound().build())
                        : Mono.just(ResponseEntity.ok(response.get(0))))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @DeleteMapping("/{collectionName}/vector/{id}")
    public Mono<ResponseEntity<String>> deleteVector(@PathVariable String collectionName, @PathVariable UUID id) {
        return qdrantService.deleteVector(collectionName, id)
                .map(result -> ResponseEntity.ok("Vector deleted for ID: " + id))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete vector: " + e.getMessage())));
    }

    @PutMapping("/{collectionName}/vector/update")
    public Mono<ResponseEntity<String>> updateVector(@PathVariable String collectionName, @RequestBody QdrantVectorRQ request) {
        return qdrantService.updateVector(collectionName, request)
                .map(result -> ResponseEntity.ok("Vector updated with ID: " + request.getId()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update vector: " + e.getMessage())));
    }

    @GetMapping("/{collectionName}/vector/count")
    public Mono<ResponseEntity<Long>> countPoints(@PathVariable String collectionName) {
        return qdrantService.pointsCount(collectionName)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}