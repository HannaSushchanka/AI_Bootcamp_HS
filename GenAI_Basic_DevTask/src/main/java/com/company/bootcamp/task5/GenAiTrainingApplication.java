package com.company.bootcamp.task5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.config.EnableWebFlux;


@SpringBootApplication
@EnableCaching
@EnableWebFlux
@PropertySource("classpath:/config/application.properties")
public class GenAiTrainingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenAiTrainingApplication.class, args);
    }

}
