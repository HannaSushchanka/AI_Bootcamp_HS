package com.company.bootcamp.task6.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;


/**
 * Configuration class for setting up the Azure OpenAI Async Client.
 * <p>
 * This configuration defines a bean that provides an asynchronous client
 * for interacting with the Azure OpenAI Service. It uses the Azure Key
 * Credential for authentication and connects to a specified endpoint.
 */
@Configuration
@Getter
public class OpenAIConfiguration {

    @Value("${client-openai-key}")
    private String openAIKey;

    @Value("${client-openai-endpoint}")
    private String endpoint;

    @Value("${client-openai-deployment-name}")
    private String deploymentName;

    @Value("${temperature}")
    private double temperature;

    @Value("${max_tokens}")
    private int maxTokens;

    @Value("${embeddings-deployment-name}")
    private String embeddingsModel;
}
