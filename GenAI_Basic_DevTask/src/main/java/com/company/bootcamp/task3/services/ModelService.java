package com.company.bootcamp.task3.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.company.bootcamp.task3.configuration.MultiModelConfiguration;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelService {

    @Autowired
    private MultiModelConfiguration multiModelConfiguration;

    public String findCapabilityByModelName(String modelName) {
        Map<String, String> capabilities = multiModelConfiguration.getCapabilities();
        if (capabilities == null) {
            log.error("Capabilities map is null");
            return "Configuration error: Capabilities not loaded";
        }
        Optional<String> capabilityFound = capabilities.entrySet().stream()
                .filter(entry -> entry.getValue().contains(modelName))
                .map(Map.Entry::getKey)
                .findFirst();

        return capabilityFound.orElse("No capability found for the model: " + modelName);
    }
}
