package com.company.bootcamp.task3.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.company.bootcamp.task3.configuration.OpenAIConfiguration;

@Service
public class DeploymentService {
    private final RestTemplate restTemplate;

    private OpenAIConfiguration configuration;

    @Autowired
    public DeploymentService(RestTemplate restTemplate, OpenAIConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public String fetchDeployments() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", configuration.getOpenAIKey());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = configuration.getEndpoint() + "/openai/models";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class);

        return response.getBody();
    }
}
