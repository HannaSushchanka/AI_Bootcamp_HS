package com.company.bootcamp.task4.utils;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

@Component
public class GenerationUtils {
    public ChatCompletionService getChatCompletionServiceBean(OpenAIAsyncClient openAIAsyncClient,
                                                              String deploymentName) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentName)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }

    public String getChatResponseAsString(ChatHistory chatHistory) {
        return chatHistory.getMessages().stream()
                .map(chatMessageContent -> chatMessageContent.getAuthorRole().name()
                        + " : " + chatMessageContent.getContent())
                .collect(Collectors.joining("\n\n"));
    }
}
