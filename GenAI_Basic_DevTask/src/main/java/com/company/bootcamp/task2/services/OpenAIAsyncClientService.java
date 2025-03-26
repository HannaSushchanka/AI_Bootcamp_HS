package com.company.bootcamp.task2.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.company.bootcamp.task2.configuration.OpenAIConfiguration;
import com.microsoft.semantickernel.connectors.ai.openai.util.ClientType;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAIClientProvider;
import com.microsoft.semantickernel.exceptions.ConfigurationException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpenAIAsyncClientService {
    private final Map<String, String> configuredSettings;

    public OpenAIAsyncClientService(OpenAIConfiguration configuration) {
        this.configuredSettings = Map.of(
                "client.openai.key", configuration.getOpenAIKey(),
                "client.openai.endpoint", configuration.getEndpoint(),
                "client.openai.deploymentname", configuration.getDeploymentName()
        );

    }


    public OpenAIAsyncClient get() {
        OpenAIClientProvider provider = new OpenAIClientProvider(configuredSettings, ClientType.OPEN_AI);
        try {
            return provider.getAsyncClient();
        } catch (ConfigurationException e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
