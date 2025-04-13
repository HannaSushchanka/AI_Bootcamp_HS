package com.company.bootcamp.task3.services;

import java.util.List;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatService {
    private final OpenAIAsyncClientService aiAsyncClientService;
    private final String deploymentOrModelName;
    private final ChatHistoryService chatHistoryService;

    public ChatService(OpenAIAsyncClientService aiAsyncClientService, ChatHistoryService chatHistoryService,
                       @Value("${client-openai-deployment-name}")String deploymentOrModelName) {
        this.aiAsyncClientService = aiAsyncClientService;
        this.deploymentOrModelName = deploymentOrModelName;
        this.chatHistoryService = chatHistoryService;
    }

    public List<String> generateResponse(String message) {
        ChatCompletions completions = aiAsyncClientService.get()
                .getChatCompletions(
                        deploymentOrModelName,
                        new ChatCompletionsOptions(
                                List.of(new ChatRequestUserMessage(message))))
                .block();
        assert completions != null;
        List<String> messages = completions.getChoices().stream()
                .map(c -> c.getMessage().getContent())
                .toList();
        log.info(messages.toString());
        return messages;
    }

    public List<String> generateMultiModelResponse(String prompt, String modelName) {
        return chatHistoryService.getGeneration(prompt, modelName, new ChatHistory());
    }
}