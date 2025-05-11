package com.company.bootcamp.task6.services;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.company.bootcamp.task6.clients.GRPSQdrantClient;
import com.company.bootcamp.task6.model.QdrantVectorRQ;
import com.microsoft.semantickernel.services.textembedding.Embedding;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
@Service
public class QdrantService {
    private final QdrantClient qdrantClient;
    private final Executor executor = Executors.newCachedThreadPool();

    public QdrantService(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    private <T> Mono<T> toMono(com.google.common.util.concurrent.ListenableFuture<T> listenableFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        listenableFuture.addListener(() -> {
            try {
                completableFuture.complete(listenableFuture.get());
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        }, executor);
        return Mono.fromFuture(completableFuture);
    }

    public Mono<Collections.CollectionOperationResponse> createCollection(String collectionName) {
        var vectorParams = Collections.VectorParams.newBuilder()
                .setDistance(Collections.Distance.Cosine)
                .setSize(1536)
                .build();

        var createCollectionRq = Collections.CreateCollection.newBuilder()
                .setCollectionName(collectionName)
                .setVectorsConfig(Collections.VectorsConfig.newBuilder().setParams(vectorParams).build())
                .build();

        return toMono(qdrantClient.createCollectionAsync(createCollectionRq));
    }

    public Mono<Collections.CollectionOperationResponse> deleteCollection(String collectionName) {
        return toMono(qdrantClient.deleteCollectionAsync(collectionName));
    }

    public Mono<Points.UpdateResult> addVector(String collectionName, QdrantVectorRQ request) {
        Points.PointStruct point = Points.PointStruct.newBuilder()
                .setId(Points.PointId.newBuilder().setUuid(request.getId().toString()).build())
                .setVectors(vectors(request.getVector()))
                .putAllPayload(request.getPayload())
                .build();

        return toMono(qdrantClient.upsertAsync(collectionName, List.of(point)));
    }

    public Mono<List<Points.RetrievedPoint>> getVector(String collectionName, UUID id) {
        Points.GetPoints getPoints = Points.GetPoints.newBuilder()
                .setCollectionName(collectionName)
                .addIds(Points.PointId.newBuilder().setUuid(id.toString()).build())
                .build();

        return toMono(qdrantClient.retrieveAsync(getPoints, null));
    }

    public Mono<Points.UpdateResult> deleteVector(String collectionName, UUID id) {
        Points.DeletePoints request = Points.DeletePoints.newBuilder()
                .setCollectionName(collectionName)
                .setPoints(Points.PointsSelector.newBuilder()
                        .setPoints(Points.PointsIdsList.newBuilder()
                                .addIds(Points.PointId.newBuilder().setUuid(id.toString()).build())
                                .build())
                        .build())
                .build();

        return toMono(qdrantClient.deleteAsync(request));
    }

    public Mono<Points.UpdateResult> updateVector(String collectionName, QdrantVectorRQ request) {
        return addVector(collectionName, request);
    }

    public Mono<Long> pointsCount(String collectionName) {
        return getCollectionInfo(collectionName)
                .map(Collections.CollectionInfo::getPointsCount);
    }

    public Mono<List<Points.ScoredPoint>> searchVectors(String collectionName, List<Embedding> embeddings) {
        if (embeddings.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Embeddings list cannot be empty"));
        }

        Points.SearchPoints searchRequest = Points.SearchPoints.newBuilder()
                .setCollectionName(collectionName)
                .addAllVector(embeddings.get(0).getVector())
                .setLimit(10)
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                .build();

        return toMono(qdrantClient.searchAsync(searchRequest));
    }

    public Mono<Collections.CollectionInfo> getCollectionInfo(String collectionName) {
        return toMono(qdrantClient.getCollectionInfoAsync(collectionName))
                .onErrorMap(e -> new RuntimeException("Failed to get collection info for collection: " + collectionName, e));
    }
}