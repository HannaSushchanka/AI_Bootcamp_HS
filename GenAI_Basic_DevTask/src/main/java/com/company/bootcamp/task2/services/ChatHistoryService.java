package com.company.bootcamp.task2.services;

import org.springframework.stereotype.Service;

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

    private ObjectMapper objectMapper;


    public String processWithHistory(String prompt, String chatHistoryAttr) {
        ChatHistory chatHistory;
        if (chatHistoryAttr == null) {
            chatHistory = new ChatHistory();
        } else {
                try {
                    chatHistory = objectMapper.readValue(chatHistoryAttr, ChatHistory.class);
                } catch (JsonProcessingException e) {
                log.error("Error deserializing chat history: {}", e.getMessage());
                chatHistory =  new ChatHistory();
            }
        }

        var response = kernel.invokeAsync(getChat())
                .withArguments(getKernelFunctionArguments(prompt, chatHistory))
                .block();

        chatHistory.addUserMessage(prompt);
        chatHistory.addAssistantMessage(response.getResult());
        log.info("AI answer:" + response.getResult());
        return response.getResult();
    }

    /**
     * Creates a kernel function for generating a chat response using a predefined prompt template.
     * <p>
     * The template includes the chat history and the user's message as variables.
     *
     * @return a {@link KernelFunction} for handling chat-based AI interactions
     */
    private KernelFunction<String> getChat() {
        return KernelFunction.<String>createFromPrompt("""
                        {{$chatHistory}}
                        <message role="user">{{$request}}</message>""")
                .build();
    }

    /**
     * Creates the kernel function arguments with the user prompt and chat history.
     *
     * @param prompt the user's input
     * @param chatHistory the current chat history
     * @return a {@link KernelFunctionArguments} instance containing the variables for the AI model
     */
    private KernelFunctionArguments getKernelFunctionArguments(String prompt, ChatHistory chatHistory) {
        return KernelFunctionArguments.builder()
                .withVariable("request", prompt)
                .withVariable("chatHistory", chatHistory)
                .build();
    }
}
