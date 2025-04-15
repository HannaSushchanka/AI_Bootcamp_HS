package com.company.bootcamp.task3.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.company.bootcamp.task3.model.ModelsRS;
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

    public Map<String, ModelsRS.ModelDetails> getModelCapabilities() throws IOException {
        ModelsRS response = objectMapper.readValue(deploymentService.fetchDeployments(), ModelsRS.class);
        Map<String, ModelsRS.ModelDetails> modelDetailsMap = new HashMap<>();

        for (ModelsRS.ModelDetails modelDetail : response.getModelDetails()) {
            modelDetailsMap.put(modelDetail.getId(), modelDetail);
        }

        return modelDetailsMap;
    }
}
