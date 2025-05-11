package com.company.bootcamp.task5.configuration.initializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Component
public class QdrantInitializer {

    private final WebClient webClient;

    // Inject properties from application.properties
    @Value("${qdrant.base-url}")
    private String baseUrl;

    @Value("${qdrant.collection.name}")
    private String collectionName;

    @Value("${qdrant.collection.vector-size}")
    private int vectorSize;

    @Value("${qdrant.collection.distance}")
    private String distance;

    public QdrantInitializer(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void initializeCollection() {
        var collectionConfig = new CollectionConfig(vectorSize, distance);

        webClient.get()
                .uri(baseUrl + "/collections/" + collectionName)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("Collection already exists: " + collectionName))
                .onErrorResume(error -> {
                    System.out.println("Collection does not exist. Creating collection: " + collectionName);
                    return createCollection(collectionConfig);
                })
                .subscribe();
    }

    private Mono<String> createCollection(CollectionConfig collectionConfig) {
        return webClient.post()
                .uri(baseUrl + "/collections/" + collectionName)
                .bodyValue(collectionConfig)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("Collection created successfully: " + response))
                .doOnError(error -> System.err.println("Failed to create collection: " + error.getMessage()));
    }

    static class CollectionConfig {
        private final Vectors vectors;

        public CollectionConfig(int size, String distance) {
            this.vectors = new Vectors(size, distance);
        }

        public Vectors getVectors() {
            return vectors;
        }

        static class Vectors {
            private final int size;
            private final String distance;

            public Vectors(int size, String distance) {
                this.size = size;
                this.distance = distance;
            }

            public int getSize() {
                return size;
            }

            public String getDistance() {
                return distance;
            }
        }
    }
}