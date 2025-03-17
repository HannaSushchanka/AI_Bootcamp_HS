package com.company.bootcamp.task1.controllers;

import com.company.bootcamp.task1.services.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class OpenAIChatController {

    @Autowired
    private OpenAIService openAIService;

//    @GetMapping("/prompt")
//    public Map<String, String> generateResponse(@RequestBody Map<String, String> request) {
//        String prompt = request.get("input");
//        String response = openAIService.generateResponseUsingPlugin(prompt);
//        return Map.of("response", response);
//    }

    @GetMapping("/prompt")
    public Map<String, String> completePrompt(@RequestParam String prompt) {
        String response = openAIService.generateResponseUsingPlugin(prompt);
        return Map.of("response", response);
    }
}
