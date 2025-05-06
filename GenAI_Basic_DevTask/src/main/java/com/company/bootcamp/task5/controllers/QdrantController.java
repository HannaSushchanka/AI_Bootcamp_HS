package com.company.bootcamp.task5.controllers;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.bootcamp.task5.model.QdrantVectorRQ;
import com.company.bootcamp.task5.services.QdrantService;

import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;

@RestController
@RequestMapping("/api/qdrant")
public class QdrantController {
    private final QdrantService qdrantService;

    public QdrantController(QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    @PostMapping("/collection/create/{name}")
    public ResponseEntity<String> createCollection(@PathVariable String name) throws ExecutionException, InterruptedException {

        Collections.CollectionOperationResponse response = qdrantService.createCollection(name);
        if (response.getResult()) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Collection created successfully " + name);
        } else {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to create " + name);
        }
    }


    @DeleteMapping("/collection/delete/{name}")
    public ResponseEntity<String> deleteCollection(@PathVariable String name) throws ExecutionException, InterruptedException {
        Collections.CollectionOperationResponse response = qdrantService.deleteCollection(name);

        if (response.getResult()) {
            return ResponseEntity.status(HttpStatus.OK).body("Collection deleted successfully " + name);
        } else {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to delete " + name);
        }
    }


    @PostMapping("/{collectionName}/vector/add")
    public ResponseEntity<String> addVector(@PathVariable String collectionName, @RequestBody QdrantVectorRQ request) throws ExecutionException, InterruptedException {
        qdrantService.addVector(collectionName, request);
        return ResponseEntity.ok("Vector inserted with ID: " + request.getId());
    }

    @GetMapping("/{collectionName}/vector/{id}")
    public ResponseEntity<Points.RetrievedPoint> getVector(@PathVariable String collectionName, @PathVariable UUID id) throws ExecutionException, InterruptedException {
        var response = qdrantService.getVector(collectionName, id);
        if (response != null && !response.get().get(0).isInitialized()) {
            return ResponseEntity.ok(response.get().get(0));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{collectionName}/vector/{id}")
    public ResponseEntity<String> deleteVector(@PathVariable String collectionName, @PathVariable UUID id) throws ExecutionException, InterruptedException {
        qdrantService.deleteVector(collectionName, id);
        return ResponseEntity.ok("Vector deleted for ID: " + id);
    }

    @PutMapping("/{collectionName}/vector/update")
    public ResponseEntity<String> updateVector(@PathVariable String collectionName, @RequestBody QdrantVectorRQ request) throws ExecutionException, InterruptedException {
        qdrantService.updateVector(collectionName, request);
        return ResponseEntity.ok("Vector inserted with ID: " + request.getId());
    }


    @GetMapping("/{collectionName}/vector/count")
    public ResponseEntity<Long> countPoints(@PathVariable String collectionName) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(qdrantService.pointsCount(collectionName));
    }
}
