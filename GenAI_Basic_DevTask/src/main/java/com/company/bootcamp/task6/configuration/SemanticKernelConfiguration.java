package com.company.bootcamp.task6.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

import com.company.bootcamp.task6.configuration.plugin.SimplePlugin;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.aiservices.openai.textembedding.OpenAITextEmbeddingGenerationService;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import lombok.AllArgsConstructor;

/**
 * Configuration class for setting up Semantic Kernel components.
 * <p>
 * This configuration provides several beans necessary for the interaction with
 * Azure OpenAI services and the creation of kernel plugins. It defines beans for
 * chat completion services, kernel plugins, kernel instance, invocation context,
 * and prompt execution settings.
 */
@Configuration
@AllArgsConstructor
public class SemanticKernelConfiguration {
    private OpenAIConfiguration configuration;

    @Bean(name = "semanticKernelOpenAIAsyncClient")
    public OpenAIAsyncClient semanticKernelOpenAIAsyncClient() {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(configuration.getOpenAIKey()))
                .endpoint(configuration.getEndpoint())
                .buildAsyncClient();
    }

    /**
     * Creates a {@link ChatCompletionService} bean for handling chat completions using Azure OpenAI.
     *
     * @param deploymentOrModelName the Azure OpenAI deployment or model name
     * @param openAIAsyncClient the {@link OpenAIAsyncClient} to communicate with Azure OpenAI
     * @return an instance of {@link ChatCompletionService}
     */
    @Bean
    public ChatCompletionService chatCompletionService(@Value("${client-openai-deployment-name}") String deploymentOrModelName,
                                                       OpenAIAsyncClient openAIAsyncClient) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentOrModelName)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }

 /**
     * Creates a {@link KernelPlugin} bean using a simple plugin.
     *
     * @return an instance of {@link KernelPlugin}
     */
    @Bean
    public KernelPlugin kernelPlugin() {
        return KernelPluginFactory.createFromObject(
                new SimplePlugin(), "Simple Plugin");
    }

    /**
     * Creates a {@link Kernel} bean to manage AI services and plugins.
     *
     * @param chatCompletionService the {@link ChatCompletionService} for handling completions
     * @param kernelPlugin the {@link KernelPlugin} to be used in the kernel
     * @return an instance of {@link Kernel}
     */
    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService, KernelPlugin kernelPlugin, KernelPlugin triathlonWorkoutsKernelPlugin) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(kernelPlugin)
                .withPlugin(triathlonWorkoutsKernelPlugin)
                .build();
    }

    /**
     * Creates an {@link InvocationContext} bean with default prompt execution settings.
     *
     * @return an instance of {@link InvocationContext}
     */
    @Bean
    public InvocationContext invocationContext() {
        return InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withTemperature(configuration.getTemperature())
                        .withMaxTokens(configuration.getMaxTokens())
                        .build())
                .build();
    }

    /**
     * Creates a map of {@link PromptExecutionSettings} for different models.
     *
     * @param deploymentOrModelName the Azure OpenAI deployment or model name
     * @return a map of model names to {@link PromptExecutionSettings}
     */
    @Bean
    public Map<String, PromptExecutionSettings> promptExecutionsSettingsMap(@Value("${client-openai-deployment-name}")
                                                                            String deploymentOrModelName) {
        return Map.of(deploymentOrModelName, PromptExecutionSettings.builder()
                .withTemperature(configuration.getTemperature())
                .withMaxTokens(configuration.getMaxTokens())
                .build());
    }

    @Bean(name = "textEmbeddingKernel")
    public Kernel textEmbeddingKernel(OpenAIAsyncClient client) {
        return Kernel.builder().withAIService(OpenAITextEmbeddingGenerationService.class, OpenAITextEmbeddingGenerationService.builder()
                .withModelId(configuration.getEmbeddingsModel()).withOpenAIAsyncClient(client).build()).build();
    }
}

