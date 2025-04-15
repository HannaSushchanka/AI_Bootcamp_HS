package com.company.bootcamp.task3.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.company.bootcamp.task3.configuration.exceptions.ModelNotFoundException;
import com.company.bootcamp.task3.model.ModelsRS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelService {

    private final DeploymentService deploymentService;
    private final ObjectMapper objectMapper;

    public ModelService(DeploymentService deploymentService, ObjectMapper objectMapper) {
        this.deploymentService = deploymentService;
        this.objectMapper = objectMapper;
    }

    @Cacheable("availableModels")
    public Map<String, ModelsRS.ModelDetails> getModelCapabilities()  {
        ModelsRS response = null;
        try {
            response = objectMapper.readValue(deploymentService.fetchDeployments(), ModelsRS.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Map<String, ModelsRS.ModelDetails> modelDetailsMap = new HashMap<>();

        if (response != null && response.getModelDetails() != null) {
            for (ModelsRS.ModelDetails modelDetail : response.getModelDetails()) {
                boolean containsAzure = modelDetail.getDescriptionKeywords().stream()
                        .anyMatch(keyword -> keyword.toLowerCase().contains("azure"));
                if (containsAzure) {
                    modelDetailsMap.put(modelDetail.getId(), modelDetail);
                }
            }
        }

        if (modelDetailsMap.isEmpty()) {
            throw new ModelNotFoundException("No supported models found.");
        }

        return modelDetailsMap;
    }
}
