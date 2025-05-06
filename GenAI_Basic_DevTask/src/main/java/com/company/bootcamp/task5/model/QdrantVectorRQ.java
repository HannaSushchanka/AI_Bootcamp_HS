package com.company.bootcamp.task5.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.qdrant.client.grpc.JsonWithInt;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QdrantVectorRQ {
    private UUID id;

    private List<Float> vector;

    private Map<String, JsonWithInt.Value> payload;
}
