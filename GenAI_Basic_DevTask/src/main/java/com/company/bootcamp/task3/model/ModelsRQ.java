package com.company.bootcamp.task3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ModelsRQ {
    private String prompt;
    private double temperature;
    private int maxTokens;
    private String modelName;
}
