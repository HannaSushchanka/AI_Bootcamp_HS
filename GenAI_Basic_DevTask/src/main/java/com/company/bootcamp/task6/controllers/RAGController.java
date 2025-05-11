package com.company.bootcamp.task6.controllers;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;


import com.company.bootcamp.task6.services.FileService;
import com.company.bootcamp.task6.services.RAGService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class RAGController {
    private FileService fileService;
    private RAGService ragService;

    @PostMapping("/uploadFile")
    public Mono<ResponseEntity<String>> uploadFile(@RequestPart FilePart file) throws IOException {
        return fileService.uploadFileData(file)
                .then(Mono.just(ResponseEntity.ok("File content uploaded.")))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to upload file: " + e.getMessage()));
                });
    }

    @GetMapping("/generate-answer")
    public Mono<String> getGeneratedAnswer(@RequestParam String userPrompt) {
        return ragService.getGeneratedAnswer(userPrompt);
    }

}
