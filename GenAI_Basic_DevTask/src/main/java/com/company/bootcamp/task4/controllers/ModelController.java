package com.company.bootcamp.task4.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.bootcamp.task4.model.ModelsRS;
import com.company.bootcamp.task4.services.ModelService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/azure-models")
@Slf4j
@AllArgsConstructor
public class ModelController {

    private ModelService modelService;
    @GetMapping
    public ResponseEntity<List<String>> generateResponse() {
        List<String> modelNames = modelService.getModelCapabilities().values().stream()
                .map(ModelsRS.ModelDetails::getModel)
                .toList();
        return ResponseEntity.ok(modelNames);
    }
}
