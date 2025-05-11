package com.company.bootcamp.task6.clients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class GRPSQdrantClient {

    private final String host;
    private final String collectionName;
    private final ObjectMapper objectMapper;


    public GRPSQdrantClient( @Value("${qdrant.base-url}") String host,
                             @Value("${qdrant.collection.name}") String collectionName) {
        this.host = host;
        this.collectionName = collectionName;
        this.objectMapper = new ObjectMapper();
    }


    public void storeEmbedding(String text, List<Double> embedding) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost( host + "/collections/" + collectionName + "/points");
            post.setHeader("Content-Type", "application/json");

            String body = String.format("{" +
                            "\"points\": [{\"id\": %d, \"vector\": %s, \"payload\": {\"text\": %s}}]" +
                            "}", UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE,
                    new ObjectMapper().writeValueAsString(embedding),
                    new ObjectMapper().writeValueAsString(text));

            post.setEntity(new StringEntity(body));
            client.execute(post).close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store embedding in Qdrant", e);
        }
    }

    public Flux<String> searchRelevantChunks(List<Double> queryEmbedding) {
        return Mono.fromCallable(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(host + "/collections/" + collectionName + "/points/search");
                post.setHeader("Content-Type", "application/json");

                // Build the request body
                String body = String.format("{" +
                        "\"vector\": %s, \"limit\": 3, \"with_payload\": true" +
                        "}", objectMapper.writeValueAsString(queryEmbedding));

                post.setEntity(new StringEntity(body));

                // Execute the HTTP request
                try (CloseableHttpResponse response = client.execute(post)) {
                    if (response.getCode() != 200) {
                        throw new RuntimeException("Failed to search in Qdrant: HTTP " + response.getCode());
                    }

                    // Parse the response
                    String json = EntityUtils.toString(response.getEntity());
                    JsonNode root = objectMapper.readTree(json);

                    // Extract results
                    List<String> results = new ArrayList<>();
                    if (root.has("result")) {
                        for (JsonNode result : root.get("result")) {
                            JsonNode payload = result.get("payload");
                            if (payload != null && payload.has("text")) {
                                results.add(payload.get("text").asText());
                            }
                        }
                    }
                    return results;
                } catch (ParseException e) {
                    throw new RuntimeException("Failed to parse response from Qdrant", e);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to search in Qdrant", e);
            }
        }).flatMapMany(Flux::fromIterable);
    }
}
