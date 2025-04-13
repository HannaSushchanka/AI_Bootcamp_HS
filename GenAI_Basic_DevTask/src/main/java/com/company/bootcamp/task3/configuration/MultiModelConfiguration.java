package com.company.bootcamp.task3.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "capabilities")
@Slf4j
public class MultiModelConfiguration {

    private Map<String, String> capabilities;

    @PostConstruct
    public void postConstruct() {
        log.info("Loaded model capabilities: {}", capabilities);
    }
}
