package com.company.bootcamp.task3.services;

import org.springframework.stereotype.Service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class ChatHistoryService {

    private Kernel kernel;
    private ObjectMapper objectMapper;
    private final OpenAIAsyncClient aiAsyncClient;


//    public String processWithHistory(String prompt, String chatHistoryJson, String modelName) throws IOException {
//        ChatHistory chatHistory = deserializeChatHistory(chatHistoryJson);
//
//        String response;
//        response = String.valueOf(getGenerationByType(prompt, modelName, chatHistory));
//
//        log.info("Response: {}", response);
//        return response;
//    }

//    /**
//     * Creates the kernel function arguments with the user prompt and chat history.
//     *
//     * @param prompt the user's input
//     * @param chatHistory the current chat history
//     * @return a {@link KernelFunctionArguments} instance containing the variables for the AI model
//     */


    private void updateChatHistory(ChatHistory chatHistory, String userMessage, String assistantMessage) {
        chatHistory.addUserMessage(userMessage);
        chatHistory.addAssistantMessage(assistantMessage);
    }

    private ChatHistory deserializeChatHistory(String chatHistoryJson) {
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


