package com.company.bootcamp.task3.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ModelsRQ {
    @NotBlank(message = "Prompt is required")
    private String prompt;
    private double temperature;
    @Positive(message = "Max tokens must be positive")
    private int maxTokens;
    @NotBlank(message = "Model name is required")
    private String modelName;
}
