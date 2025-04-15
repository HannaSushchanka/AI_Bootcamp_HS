package com.company.bootcamp.task3.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.company.bootcamp.task3.services.DeploymentService;

@Component
public class FetchingDIALModelsRunner implements CommandLineRunner {

    @Autowired
    private DeploymentService deploymentService;

    @Override
    public void run(String... args) throws Exception {
        deploymentService.fetchDeployments();
    }
}
