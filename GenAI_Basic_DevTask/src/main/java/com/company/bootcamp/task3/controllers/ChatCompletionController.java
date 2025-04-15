package com.company.bootcamp.task3.controllers;

import com.company.bootcamp.task3.model.ModelsRQ;
import com.company.bootcamp.task3.services.ChatService;
import com.company.bootcamp.task3.services.ChatHistoryService;
import com.company.bootcamp.task3.services.ModelSettingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@Slf4j
@AllArgsConstructor
public class ChatCompletionController {

    private ChatService chatService;

    private ChatHistoryService chatHistoryService;
    private final ModelSettingService modelSettingsService;

    @GetMapping
    public ResponseEntity<List<String>> generateResponse(@RequestParam String prompt) {
        log.info("prompt value: {}", prompt);
        return ResponseEntity.ok(chatService.generateResponse(prompt));
    }

//    @GetMapping("/history")
//    public Mono<ResponseEntity<String>> getResponse(@RequestParam String prompt, @RequestParam String modelName, ServerWebExchange exchange) {
//        log.info("Received prompt: {}", prompt);
//        return exchange.getSession()
//                .flatMap(session -> {
//                    String chatHistory = session.getAttribute("chatHistory");
//                    return Mono.fromCallable(() -> chatHistoryService.processWithHistory(prompt, chatHistory,modelName));
//                })
//                .map(ResponseEntity::ok);
//    }

    @GetMapping("/multi-model-chat")
    public ResponseEntity<String> getMultiModelResponse(@RequestBody ModelsRQ modelsRQ) {
        try {
            return ResponseEntity.ok(chatService.generateMultiModelResponse(modelsRQ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        }
    }

}
