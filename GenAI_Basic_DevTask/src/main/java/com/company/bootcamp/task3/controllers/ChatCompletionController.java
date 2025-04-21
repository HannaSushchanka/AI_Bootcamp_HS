package com.company.bootcamp.task3.controllers;

import com.company.bootcamp.task3.model.ModelsRQ;
import com.company.bootcamp.task3.services.ChatService;
import com.company.bootcamp.task3.services.ChatHistoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;


import java.util.List;
import java.util.Map;

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
        return Mono.just(prompt)
                .flatMap(chatService::generateResponse)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(null)));
    }


    @GetMapping("/multi-model-chat")
    public Mono<ResponseEntity<String>> startOrContinueChat(@RequestBody ModelsRQ modelsRQ, @RequestParam(required = false) final String chatId,
                                                            ServerWebExchange exchange) {

        if (modelsRQ.getPrompt() == null || modelsRQ.getModelName() == null) {
            return Mono.just(ResponseEntity.badRequest().body("Prompt and modelName are required"));
        }

        return exchange.getSession()
                .flatMap(session -> {
                    Map<String, Object> sessionAttributes = session.getAttributes();
                    String effectiveChatId = chatId != null ? chatId : chatHistoryService.generateChatId();

                    return chatHistoryService.getOrCreateChatHistory(sessionAttributes, effectiveChatId)
                            .flatMap(chatHistory -> chatService.generateMultiModelResponse(modelsRQ, chatHistory))
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Error processing request: " + e.getMessage())));
                })
                .defaultIfEmpty(ResponseEntity.badRequest().body("Failed to process chat message"));
    }

    @DeleteMapping("/clear-history/{chatId}")
    public Mono<ResponseEntity<Void>> clearChatHistory(@PathVariable String chatId, ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    Map<String, Object> sessionAttributes = session.getAttributes();
                    return chatHistoryService.clearChatHistory(sessionAttributes, chatId);
                })
                .then(Mono.just(ResponseEntity.ok().build()));
    }

}
