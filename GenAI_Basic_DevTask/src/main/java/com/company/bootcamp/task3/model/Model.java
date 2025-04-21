package com.company.bootcamp.task3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Model {
    private String id;
    private String model;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("description_keywords")
    private List<String> descriptionKeywords;
}
