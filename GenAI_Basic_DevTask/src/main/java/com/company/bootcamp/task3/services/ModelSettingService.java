package com.company.bootcamp.task3.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;

@Service
public class ModelSettingService {
    private final Map<String, PromptExecutionSettings> settingsMap = new ConcurrentHashMap<>();

    public void updateModelSettings(String modelName, double temperature, int maxTokens) {
        PromptExecutionSettings settings = PromptExecutionSettings.builder()
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .build();
        settingsMap.put(modelName, settings);
    }

    public PromptExecutionSettings getSettings(String modelName) {
        return settingsMap.get(modelName);
    }
}
