package com.company.bootcamp.task3.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class ChatHistoryService {

    private Kernel kernel;
    private ObjectMapper objectMapper;
    private final OpenAIAsyncClient aiAsyncClient;

    private static final String CHAT_HISTORIES = "chatHistories";

    public Mono<Map<String, ChatHistory>> getChatHistories(Map<String, Object> sessionAttributes) {
        @SuppressWarnings("unchecked")
        Map<String, ChatHistory> chatHistories = (Map<String, ChatHistory>) sessionAttributes.get(CHAT_HISTORIES);
        if (chatHistories == null) {
            chatHistories = new ConcurrentHashMap<>();
            sessionAttributes.put(CHAT_HISTORIES, chatHistories);
        }
        return Mono.just(chatHistories);
    }

    public Mono<Void> saveChatHistory(Map<String, Object> sessionAttributes, String chatId, ChatHistory chatHistory) {
        @SuppressWarnings("unchecked")
        Map<String, ChatHistory> chatHistories = (Map<String, ChatHistory>) sessionAttributes.get(CHAT_HISTORIES);
        if (chatHistories == null) {
            chatHistories = new ConcurrentHashMap<>();
            sessionAttributes.put(CHAT_HISTORIES, chatHistories);
        }
        chatHistories.put(chatId, chatHistory);
        return Mono.empty();
    }

    public Mono<Void> clearChatHistory(Map<String, Object> sessionAttributes, String chatId) {
        @SuppressWarnings("unchecked")
        Map<String, ChatHistory> chatHistories = (Map<String, ChatHistory>) sessionAttributes.get(CHAT_HISTORIES);
        if (chatHistories != null) {
            chatHistories.remove(chatId);
        }
        return Mono.empty();
    }

    public String generateChatId() {
        return UUID.randomUUID().toString();
    }

    public Mono<ChatHistory> getOrCreateChatHistory(Map<String, Object> sessionAttributes, String chatId) {
        @SuppressWarnings("unchecked")
        Map<String, ChatHistory> histories = (Map<String, ChatHistory>) sessionAttributes.get(CHAT_HISTORIES);
        if (histories == null) {
            histories = new ConcurrentHashMap<>();
            sessionAttributes.put(CHAT_HISTORIES, histories);
        }
        return Mono.just(histories.computeIfAbsent(chatId, k -> new ChatHistory()));
    }

}


