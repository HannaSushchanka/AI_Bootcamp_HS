package com.company.bootcamp.task2.controllers;

import com.company.bootcamp.task2.services.ChatService;
import com.company.bootcamp.task2.services.ChatHistoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

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

    @GetMapping
    public Mono<ResponseEntity<List<String>>> generateResponse(@RequestParam String prompt) {
        log.info("prompt value: {}", prompt);
        return Mono.just(chatService.generateResponse(prompt))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/history")
    public Mono<ResponseEntity<String>> getResponse(@RequestParam String prompt, ServerWebExchange exchange) {
        log.info("Received prompt: {}", prompt);
        return exchange.getSession()
                .flatMap(session -> {
                    String chatHistory = session.getAttribute("chatHistory");
                    return Mono.fromCallable(() -> chatHistoryService.processWithHistory(prompt, chatHistory));
                })
                .map(ResponseEntity::ok);
    }


}
