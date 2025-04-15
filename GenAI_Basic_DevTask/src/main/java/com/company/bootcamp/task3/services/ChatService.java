package com.company.bootcamp.task3.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ImageGenerationData;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageGenerationQuality;
import com.azure.ai.openai.models.ImageGenerations;
import com.azure.ai.openai.models.ImageSize;
import com.company.bootcamp.task3.configuration.OpenAIConfiguration;
import com.company.bootcamp.task3.model.ModelsRQ;
import com.company.bootcamp.task3.model.ModelsRS;
import com.company.bootcamp.task3.utils.GenerationUtils;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;

import com.microsoft.semantickernel.orchestration.responseformat.ResponseFormat;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatService {
    private final OpenAIAsyncClientService aiAsyncClientService;
    private final String deploymentOrModelName;
    private final ModelService modelService;
    private OpenAIAsyncClient openAIAsyncClient;
    private Kernel kernel;
    private ChatHistory chatHistory;
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
        this.chatHistory = chatHistory;
        this.configuration = configuration;
        this.openAIAsyncClient = openAiAsyncClient;
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

    public String generateMultiModelResponse(ModelsRQ modelsRQ) throws IOException {
        Map<String, ModelsRS.ModelDetails> modelDetailsMap = modelService.getModelCapabilities();

        ModelsRS.ModelDetails modelDetails = modelDetailsMap.get(modelsRQ.getModelName());
        if (modelDetails == null) {
            return "Model not found";
        }

        boolean hasTextCapability = modelDetails.getDescriptionKeywords().stream()
                .anyMatch(keyword -> keyword.toLowerCase().contains("text"));
        boolean hasImageCapability = modelDetails.getDescriptionKeywords().stream()
                .anyMatch(keyword -> keyword.toLowerCase().contains("image"));

//        if (hasTextCapability && hasImageCapability) {
//            return "text & image";
//        } else
        if (hasTextCapability) {
            return processText(modelsRQ);
        } else if (hasImageCapability) {
            return processText(modelsRQ);
        } else {
            return "no specific capabilities";
        }
    }

    private String processText(ModelsRQ modelsRQ) {
        chatHistory.addUserMessage(modelsRQ.getPrompt());

        InvocationContext invocationContext = InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withModelId(modelsRQ.getModelName())
                        .withTemperature(modelsRQ.getTemperature() != 0 ? modelsRQ.getTemperature() : configuration.getTemperature())
                        .withMaxTokens(modelsRQ.getMaxTokens() != 0 ? modelsRQ.getMaxTokens() : configuration.getMaxTokens())
                        .withResponseFormat(ResponseFormat.Type.TEXT)
                        .build())
                .build();
        List<ChatMessageContent<?>> chatResponse = generationUtils
                .getChatCompletionServiceBean(openAIAsyncClient, modelsRQ.getModelName()).getChatMessageContentsAsync(
                        chatHistory,
                        kernel,
                        invocationContext
                ).onErrorMap(ex -> new Exception(ex.getMessage())).block();

        assert chatResponse != null;
        chatHistory.addSystemMessage(chatResponse.stream()
                .map(ChatMessageContent::getContent)
                .collect(Collectors.joining(" ")));

        return generationUtils.getChatResponseAsString(chatHistory);
    }

    private String processImage(ModelsRQ modelsRQ) {
        ImageGenerationOptions options = new ImageGenerationOptions(modelsRQ.getModelName())
                .setN(1)
                .setQuality(ImageGenerationQuality.HD)
                .setSize(ImageSize.SIZE1024X1024);
        ImageGenerations imageGenerations = openAIAsyncClient.getImageGenerations(modelsRQ.getPrompt(), options).block();
        assert imageGenerations != null;
        return imageGenerations.getData().stream()
                .map(ImageGenerationData::getUrl).toString();
    }

    protected KernelFunctionArguments createKernelFunctionArguments(String prompt) {
        return KernelFunctionArguments.builder()
                .withVariable("request", prompt)
                .build();
    }

    private KernelFunction<String> createChatKernelFunction() {
        return KernelFunction.<String>createFromPrompt("""
                        {{$chatHistory}}
                        <message role="user">{{$request}}</message>""")
                .build();
    }
}