package com.company.bootcamp.task3.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ModelsRS {
    @JsonProperty("data")
    private List<ModelDetails> modelDetails;
    private String object;

    @Setter
    @Getter
    public static class ModelDetails {
        private String id;
        private String model;
        @JsonProperty("description_keywords")
        private List<String> descriptionKeywords;
    }
}