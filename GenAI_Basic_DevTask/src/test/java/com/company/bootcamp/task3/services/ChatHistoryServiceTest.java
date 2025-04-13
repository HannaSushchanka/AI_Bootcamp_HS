package com.company.bootcamp.task3.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ImageGenerationData;
import com.azure.ai.openai.models.ImageGenerationOptions;
import com.azure.ai.openai.models.ImageGenerations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;


@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceTest {

    @Mock
    private Kernel kernel;

    @Mock
    private ModelService modelService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OpenAIAsyncClient aiAsyncClient;

    @InjectMocks
    private ChatHistoryService chatHistoryService;

    @Test
    void testProcessWithHistory_ImageCapability() {
        String prompt = "Generate an image";
        String modelName = "imageModel";

        // Mocking ImageGenerationData
        ImageGenerationData imageData = mock(ImageGenerationData.class);
        when(imageData.getUrl()).thenReturn("http://example.com/image1.jpg");

        List<ImageGenerationData> imageDataList = Collections.singletonList(imageData);

        // Mocking ImageGenerations
        ImageGenerations imageGenerations = mock(ImageGenerations.class);
        when(imageGenerations.getData()).thenReturn(imageDataList);

        when(modelService.findCapabilityByModelName(modelName)).thenReturn("image");
        when(aiAsyncClient.getImageGenerations(anyString(), any(ImageGenerationOptions.class))).thenReturn(Mono.just(imageGenerations));

        String result = chatHistoryService.processWithHistory(prompt, null, modelName);

        assertTrue(result.contains("http://example.com/image1.jpg"));
        verify(aiAsyncClient, times(1)).getImageGenerations(anyString(), any(ImageGenerationOptions.class));
    }
}