package com.company.bootcamp.task3.services;


import java.util.List;

import java.util.Map;
import java.util.stream.Collectors;

import com.azure.ai.openai.OpenAIAsyncClient;

import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;


import com.company.bootcamp.task3.configuration.OpenAIConfiguration;
import com.company.bootcamp.task3.configuration.exceptions.ModelNotFoundException;
import com.company.bootcamp.task3.model.ModelsRQ;
import com.company.bootcamp.task3.utils.GenerationUtils;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;

import com.microsoft.semantickernel.orchestration.responseformat.ResponseFormat;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ChatService {
    private final OpenAIAsyncClientService aiAsyncClientService;
    private final String deploymentOrModelName;
    private final ModelService modelService;
    private OpenAIAsyncClient openAIAsyncClient;
    private Kernel kernel;
    private OpenAIConfiguration configuration;
    @Autowired
    private GenerationUtils generationUtils;

    public ChatService(OpenAIAsyncClientService aiAsyncClientService, ModelService modelService,
                       @Value("${client-openai-deployment-name}") String deploymentOrModelName,
                       Kernel kernel, ChatHistory chatHistory, OpenAIConfiguration configuration,
                       @Qualifier("semanticKernelOpenAIAsyncClient") OpenAIAsyncClient openAiAsyncClient) {
        this.aiAsyncClientService = aiAsyncClientService;
        this.deploymentOrModelName = deploymentOrModelName;
        this.modelService = modelService;
        this.kernel = kernel;
        this.configuration = configuration;
        this.openAIAsyncClient = openAiAsyncClient;
    }

    public Mono<List<String>> generateResponse(String message) {
        return aiAsyncClientService.get()
                .getChatCompletions(deploymentOrModelName, new ChatCompletionsOptions(List.of(new ChatRequestUserMessage(message))))
                .map(completions -> completions.getChoices().stream()
                        .map(c -> c.getMessage().getContent())
                        .toList())
                .doOnSuccess(messages -> log.info(messages.toString()));
    }

    public Mono<String> generateMultiModelResponse(ModelsRQ modelsRQ, ChatHistory chatHistory) {
        String modelName = modelsRQ.getModelName();

        chatHistory.addUserMessage(modelsRQ.getPrompt());

        InvocationContext invocationContext = InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withModelId(modelName)
                        .withTemperature(modelsRQ.getTemperature() != 0 ? modelsRQ.getTemperature() : configuration.getTemperature())
                        .withMaxTokens(modelsRQ.getMaxTokens() != 0 ? modelsRQ.getMaxTokens() : configuration.getMaxTokens())
                        .withResponseFormat(ResponseFormat.Type.TEXT)
                        .build())
                .build();

        return Mono.fromSupplier(modelService::getModelCapabilities)
                .flatMap(modelsMap -> {
                    if (!modelsMap.containsKey(modelName)) {
                        log.error(modelName + " - No supported models found, throwing exception.");
                        return Mono.error(new ModelNotFoundException("Model not found: " + modelName));
                    }
                    return generationUtils.getChatCompletionServiceBean(openAIAsyncClient, modelName)
                            .getChatMessageContentsAsync(chatHistory, kernel, invocationContext)
                            .map(chatResponse -> {
                                chatHistory.addSystemMessage(chatResponse.stream()
                                        .map(ChatMessageContent::getContent)
                                        .collect(Collectors.joining(" ")));
                                return generationUtils.getChatResponseAsString(chatHistory);
                            });
                });
    }
}