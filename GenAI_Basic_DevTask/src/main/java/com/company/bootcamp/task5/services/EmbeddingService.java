package com.company.bootcamp.task5.services;

import com.company.bootcamp.task5.model.QdrantVectorRQ;
import com.company.bootcamp.task5.model.QdrantVectorRS;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.textembedding.Embedding;

import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.PointStruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;


@Slf4j
@Service
public class EmbeddingService {
    private static final String COLLECTION_NAME = "openai_vectors";

    private final QdrantService qdrantService;
    private final Kernel kernel;

    public EmbeddingService(@Qualifier("textEmbeddingKernel") Kernel kernel, QdrantService qdrantService) {
        this.kernel = kernel;
        this.qdrantService = qdrantService;
    }

    public List<Embedding> build(String text) {
        OpenAITextEmbeddingGenerationService embeddingGenerationService = null;
        try {
            embeddingGenerationService = kernel.getService(OpenAITextEmbeddingGenerationService.class);
        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
        List<Embedding> generatedEmbeddingsAsync = embeddingGenerationService.generateEmbeddingsAsync(List.of(text))
                .block();

        assert generatedEmbeddingsAsync != null;
        generatedEmbeddingsAsync.forEach(embedding -> log.info("** {}", embedding.getVector()));
        return generatedEmbeddingsAsync;
    }


    public void store(List<Map<String, Object>> input) {
        input.forEach(stringObjectMap -> {
            Map<String, JsonWithInt.Value> payload = generatePayload(stringObjectMap);
            String prompt = getGenericSummarizedString(payload);
            List<Embedding> embeddings = build(prompt);
            List<Points.UpdateResult> response = embeddings.stream().map(embedding -> {
                QdrantVectorRQ request = QdrantVectorRQ.builder()
                        .id(UUID.randomUUID())
                        .vector(embedding.getVector())
                        .payload(payload)
                        .build();
                try {
                    Points.UpdateResult result = qdrantService.addVector(COLLECTION_NAME, request).get();
                    log.info("Vector saved with Id : {}", result.getOperationId());
                    return result;
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        });
    }


    public List<QdrantVectorRS> search(String prompt) {
        List<Embedding> embeddings = build(prompt);
        List<Points.ScoredPoint> response = null;
        try {
            response = qdrantService.searchVectors(COLLECTION_NAME, embeddings);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.stream().map(scoredPoint -> QdrantVectorRS.builder()
                .id(scoredPoint.getId().getUuid())
                .payload(scoredPoint.getPayloadMap().entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, o -> o.getValue()
                                .toString().split(":")[1].trim().replace("\"", ""))))
                .similarityScore(scoredPoint.getScore())
                .version(scoredPoint.getVersion())
                .build()).toList();
    }


    /**
     * Constructs a point structure from a list of float values representing a vector.
     *
     * @param point the vector values
     * @return a {@link PointStruct} object containing the vector and associated metadata
     */
    private PointStruct getPointStruct(List<Float> point) {
        return PointStruct.newBuilder()
                .setId(id(1))
                .setVectors(vectors(point))
                .putAllPayload(Map.of("info", value("Some info")))
                .build();
    }

    private Map<String, JsonWithInt.Value> generatePayload(Map<String, Object> input) {
        Map<String, JsonWithInt.Value> payload = new HashMap<>();
        input.forEach((key, value) -> payload.put(key, toValue(value)));
        return payload;
    }

    private JsonWithInt.Value toValue(Object obj) {
        JsonWithInt.Value.Builder builder = JsonWithInt.Value.newBuilder();
        switch (obj) {
            case String str -> builder.setStringValue(str);
            case Integer i -> builder.setIntegerValue(i);
            case Double d -> builder.setDoubleValue(d);
            case Float f -> builder.setDoubleValue(f.doubleValue());
            case Boolean b -> builder.setBoolValue(b);
            case Long l -> builder.setIntegerValue(l.intValue());
            default -> builder.setStringValue(obj.toString());
        }
        return builder.build();
    }


    private String getGenericSummarizedString(Map<String, JsonWithInt.Value> payload) {
        StringBuilder summary = new StringBuilder();
        // Sort keys to ensure consistent order
        payload.keySet().stream().sorted().forEach(key -> {
            Object value = payload.get(key);
            String displayValue = value.toString().trim();
            // Use "key=value" format for clarity and consistency
            summary.append(key.toLowerCase()).append("=").append(displayValue).append(";");
        });
        String result = summary.toString().trim();
        log.info("Standardized summary string: {}", result);
        return result;
    }
}
