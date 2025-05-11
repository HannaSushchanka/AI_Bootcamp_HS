package com.company.bootcamp.task6.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class FileService {
    private EmbeddingService embeddingService;

    public Mono<Void> uploadFileData(FilePart file) {
        return file.content() // Get the file content as a Flux<DataBuffer>
                .map(dataBuffer -> {
                    // Convert DataBuffer to String
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .reduce((content1, content2) -> content1 + content2)
                .flatMapMany(content -> {
                    // Normalize line endings and split into chunks
                    String normalizedContent = content.replace("\r\n", "\n");
                    List<String> chunks = Arrays.asList(normalizedContent.split("(?<=\n\n)"));
                    return Flux.fromIterable(chunks); // Convert chunks into a Flux
                })
                .doOnNext(chunk -> System.out.println("Processing chunk: " + chunk))
                .collectList()
                .flatMap(chunks -> embeddingService.getEmbeddingAndStore(chunks))
                .then();
    }
}
