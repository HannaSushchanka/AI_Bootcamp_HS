package com.company.bootcamp.task5.services;

import com.azure.ai.openai.models.Embeddings;
import com.microsoft.semantickernel.services.textembedding.Embedding;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Points;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.company.bootcamp.task5.configuration.OpenAIClient;

import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Service
public class EmbeddingService {
    private static final String COLLECTION_NAME = "openai_vectors";

    private final QdrantClient qdrantClient;

    @Autowired
    private OpenAIClient openAIClient;

    @Autowired
    public EmbeddingService(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    public Embeddings generateEmbedding(String text) {
        Embeddings embeddings = openAIClient.getEmbeddings(text);
        if (embeddings.getData().isEmpty()) {
            throw new IllegalArgumentException("No embeddings found for the provided text.");
        }
        return embeddings;
    }

    @SneakyThrows
    public Embeddings storeEmbedding(final Embeddings embeddings, String input) {
        List<Float> embedding = new ArrayList<>(embeddings.getData()
                .stream().findFirst()
                .get().getEmbedding());
        Points.PointStruct pointStruct = Points.PointStruct.newBuilder()
                .setId(PointIdFactory.id(UUID.randomUUID()))
                .setVectors(VectorsFactory.vectors(embedding))
                .putAllPayload(Map.of("info", ValueFactory.value(input)))
                .build();
        qdrantClient.upsertAsync(COLLECTION_NAME,List.of(pointStruct)).get();

        return embeddings;
    }


    @SneakyThrows
    public List<String> searchClosestEmbeddings(List<Float> embedding, int topK) {
        System.out.println("Searching for closest embeddings for vector: " + embedding);
        List<Points.ScoredPoint> scoredPoints = searchVectors(List.of(new Embedding(embedding)), topK);
        return scoredPoints.stream()
                .map(scoredPoint -> scoredPoint.getId().toString())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private List<Points.ScoredPoint> searchVectors(List<Embedding> embeddings, int topK) {
        List<Points.ScoredPoint> result = qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .addAllVector(embeddings.getFirst().getVector())
                .setLimit(topK)
                .setWithPayload(enable(true))
                .build()).get();
        return result;
    }
}
