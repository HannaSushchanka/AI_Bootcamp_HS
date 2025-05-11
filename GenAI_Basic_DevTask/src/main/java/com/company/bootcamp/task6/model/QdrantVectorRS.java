package com.company.bootcamp.task6.model;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QdrantVectorRS {
    private String id;

    private Map<String, Object> payload;

    private Float similarityScore;

    private long version;
}
