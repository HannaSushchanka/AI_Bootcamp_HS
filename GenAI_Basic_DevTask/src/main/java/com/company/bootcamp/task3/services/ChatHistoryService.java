package com.company.bootcamp.task3.services;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ImageGenerationData;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageGenerationQuality;
import com.azure.ai.openai.models.ImageGenerations;
import com.azure.ai.openai.models.ImageSize;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class ChatHistoryService {

    private Kernel kernel;
    private ModelService modelService;
    private ObjectMapper objectMapper;
    private final OpenAIAsyncClient aiAsyncClient;


    public String processWithHistory(String prompt, String chatHistoryJson, String modelName) {
        ChatHistory chatHistory = deserializeChatHistory(chatHistoryJson);

        String response;
        response = String.valueOf(getGeneration(prompt, modelName, chatHistory));

        log.info("Response: {}", response);
        return response;
    }

    List<String> getGeneration(String prompt, String modelName, ChatHistory chatHistory) {
        String capability = "image"; //modelService.findCapabilityByModelName(modelName);
        return switch (capability) {
            case "text" -> processText(prompt, chatHistory);
            case "image" -> processImage(prompt, modelName);
            default -> List.of("Capability not supported");
        };
    }

    private List<String> processText(String prompt, ChatHistory chatHistory) {
        KernelFunction<String> chatFunction = createChatKernelFunction();
        KernelFunctionArguments arguments = createKernelFunctionArguments(prompt, chatHistory);

        String response = Objects.requireNonNull(kernel.invokeAsync(chatFunction)
                        .withArguments(arguments)
                        .block())
                .getResult();

        updateChatHistory(chatHistory, prompt, response);
        log.info("AI answer: {}", response);
        return List.of(response);
    }

    private List<String> processImage(String prompt, String modelName) {
        ImageGenerationOptions options = new ImageGenerationOptions(modelName)
                .setN(1)
                .setQuality(ImageGenerationQuality.HD)
                .setSize(ImageSize.SIZE1024X1024);
        ImageGenerations imageGenerations = aiAsyncClient.getImageGenerations(prompt, options).block();
        assert imageGenerations != null;
        return imageGenerations.getData().stream()
                .map(ImageGenerationData::getUrl)
                .toList();
    }

    /**
     * Creates the kernel function arguments with the user prompt and chat history.
     *
     * @param prompt the user's input
     * @param chatHistory the current chat history
     * @return a {@link KernelFunctionArguments} instance containing the variables for the AI model
     */
    protected KernelFunctionArguments createKernelFunctionArguments(String prompt, ChatHistory chatHistory) {
        return KernelFunctionArguments.builder()
                .withVariable("request", prompt)
                .withVariable("chatHistory", chatHistory)
                .build();
    }

    private KernelFunction<String> createChatKernelFunction() {
        return KernelFunction.<String>createFromPrompt("""
                        {{$chatHistory}}
                        <message role="user">{{$request}}</message>""")
                .build();
    }

    private void updateChatHistory(ChatHistory chatHistory, String userMessage, String assistantMessage) {
        chatHistory.addUserMessage(userMessage);
        chatHistory.addAssistantMessage(assistantMessage);
    }private ChatHistory deserializeChatHistory(String chatHistoryJson) {
        if (chatHistoryJson == null) {
            return new ChatHistory();
        }
        try {
            return objectMapper.readValue(chatHistoryJson, ChatHistory.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing chat history: {}", e.getMessage());
            return new ChatHistory();
        }
    }
}


