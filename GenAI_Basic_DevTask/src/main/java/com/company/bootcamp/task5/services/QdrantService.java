package com.company.bootcamp.task5.services;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.company.bootcamp.task5.model.QdrantVectorRQ;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.semantickernel.services.textembedding.Embedding;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;

import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Slf4j
@Service
public class QdrantService {
    private final QdrantClient qdrantClient;

    public QdrantService(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    public Collections.CollectionOperationResponse createCollection(String collectionName) throws ExecutionException, InterruptedException {
        var vectorParams = Collections.VectorParams.newBuilder()
                .setDistance(Collections.Distance.Cosine)
                .setSize(1536)
                .build();
        var createCollectionRq = Collections
                .CreateCollection.newBuilder()
                .setCollectionName(collectionName)
                .setVectorsConfig(
                        Collections.VectorsConfig.newBuilder()
                                .setParams(vectorParams).build())
                .build();
        var result = qdrantClient.createCollectionAsync(createCollectionRq);
        return result.get();
    }

    public Collections.CollectionOperationResponse deleteCollection(String collectionName) throws ExecutionException, InterruptedException {
        var future = qdrantClient.deleteCollectionAsync(collectionName);
        return future.get();
    }


    public ListenableFuture<Points.UpdateResult> addVector(String collectionName, QdrantVectorRQ request) throws ExecutionException, InterruptedException {
        Points.PointStruct point = Points.PointStruct.newBuilder()
                .setId(Points.PointId.newBuilder().setUuid(request.getId().toString()).build())
                .setVectors(vectors(request.getVector()))
                .putAllPayload(request.getPayload())
                .build();

        return qdrantClient.upsertAsync(collectionName, List.of(point));
    }

    public ListenableFuture<List<Points.RetrievedPoint>> getVector(String collectionName, UUID id) {
        Points.GetPoints getPoints = Points.GetPoints.newBuilder()
                .setCollectionName(collectionName)
                .addIds(Points.PointId.newBuilder().setUuid(String.valueOf(id)).build())
                .build();
        return qdrantClient.retrieveAsync(getPoints, null);
    }

    public void deleteVector(String collectionName, UUID id) throws ExecutionException, InterruptedException {
        Points.DeletePoints request = Points.DeletePoints.newBuilder()
                .setCollectionName(collectionName)
                .setPoints(Points.PointsSelector.newBuilder()
                        .setPoints(Points.PointsIdsList.newBuilder()
                                .addIds(Points.PointId.newBuilder().setUuid(String.valueOf(id)).build())
                                .build()).build())
                .build();
        qdrantClient.deleteAsync(request).get();
    }

    public void updateVector(String collectionName, QdrantVectorRQ request) throws ExecutionException, InterruptedException {
        addVector(collectionName, request);
    }

    public Long pointsCount(String collectionName) throws ExecutionException, InterruptedException {
        return qdrantClient.getCollectionInfoAsync(collectionName)
                .get().getPointsCount();
    }


    public List<Points.ScoredPoint> searchVectors(String collectionName, List<Embedding> embeddings) throws ExecutionException, InterruptedException {
        return qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                .setCollectionName(collectionName)
                .addAllVector(embeddings.getFirst().getVector())
                .setLimit(10)
                .setWithPayload(enable(true))
                .build()).get();
    }
}
