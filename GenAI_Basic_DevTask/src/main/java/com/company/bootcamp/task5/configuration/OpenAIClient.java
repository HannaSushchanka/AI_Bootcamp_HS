package com.company.bootcamp.task5.configuration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.*;
import io.qdrant.client.QdrantClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OpenAIClient {
    private static final String COLLECTION_NAME = "embedding_text";

    @Autowired
    private OpenAIConfiguration configuration;

    @Autowired
    private OpenAIAsyncClient openAIAsyncClient;

    @Autowired
    private QdrantClient qdrantClient;

    public List<String> chat(final String input) {
        ChatCompletions chatCompletions = openAIAsyncClient.getChatCompletions(configuration.getDeploymentName(),
                        new ChatCompletionsOptions(List.of(new ChatRequestUserMessage(input))))
                .block();

        return Optional.ofNullable(chatCompletions)
                .map(ChatCompletions::getChoices)
                .orElse(Collections.emptyList())
                .stream()
                .map(ChatChoice::getMessage)
                .map(ChatResponseMessage::getContent)
                .toList();
    }

    public Embeddings getEmbeddings(final String input) {
        return openAIAsyncClient.getEmbeddings(configuration.getEmbeddingsModel(),
                new EmbeddingsOptions(List.of(input))).block();
    }
}
