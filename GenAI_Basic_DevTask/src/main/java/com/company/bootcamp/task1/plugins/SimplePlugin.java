package com.company.bootcamp.task1.plugins;

import com.azure.ai.openai.OpenAIAsyncClient;

import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * A simple plugin that defines a kernel function for performing a basic action on data.
 * <p>
 * This plugin exposes a method to be invoked by the kernel, which logs and returns the input query.
 */
@Slf4j
public class SimplePlugin {

    private final OpenAIAsyncClient openAIAsyncClient;

    public SimplePlugin(OpenAIAsyncClient openAIAsyncClient) {
        this.openAIAsyncClient = openAIAsyncClient; // Inject OpenAIAsyncClient
    }

    @DefineKernelFunction(name = "processInputAndGenerateResponse", description = "Process input and respond")
    public String processInputAndGenerateResponse(
            @KernelFunctionParameter(description = "Prompt", name = "prompt") String prompt) {
        try {
            log.info("Calling OpenAI with prompt: [{}]", prompt);

            // Prepare ChatMessage with the user prompt
            ChatRequestMessage userMessage = new ChatRequestMessage();

            // Define ChatCompletionsOptions
            ChatCompletionsOptions options = new ChatCompletionsOptions(List.of(userMessage))
                    .setMaxTokens(100) // Limit the response length
                    .setTemperature(0.7); // Set creativity level

            // Call OpenAI asynchronously
            return openAIAsyncClient.getChatCompletions("gpt-3.5-turbo", options)
                    .map(result -> result.getChoices().get(0).getMessage().getContent()) // Extract the first choice's response
                    .block(); // Block to make the call synchronous (you could also handle asynchronously)
        } catch (Exception e) {
            log.error("Error calling OpenAI: {}", e.getMessage(), e);
            return "Error processing input: " + e.getMessage();
        }
    }
}
