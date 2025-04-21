package com.company.bootcamp.task4.configuration.exceptions;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return switch (ex) {
            case IOException ioEx -> handleIOException(ioEx, exchange);
            case RestClientException restEx -> handleRestClientException(restEx, exchange);
            case ModelNotFoundException modelEx -> handleModelNotFoundException(modelEx, exchange);
            default -> handleGeneralException(ex, exchange);
        };
    }

    private Mono<Void> handleIOException(IOException ex, ServerWebExchange exchange) {
        log.error("IOException occurred: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error processing the request: " + ex.getMessage())
                .flatMap(response -> response.writeTo(exchange, null));
    }

    private Mono<Void> handleRestClientException(RestClientException ex, ServerWebExchange exchange) {
        log.error("RestClientException occurred: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyValue("Service is temporarily unavailable: " + ex.getMessage())
                .flatMap(response -> response.writeTo(exchange, null));
    }

    private Mono<Void> handleModelNotFoundException(ModelNotFoundException ex, ServerWebExchange exchange) {
        log.error("ModelNotFoundException occurred: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .bodyValue("Model not found: " + ex.getMessage())
                .flatMap(response -> response.writeTo(exchange, null));
    }

    private Mono<Void> handleGeneralException(Throwable ex, ServerWebExchange exchange) {
        log.error("Exception occurred: {}", ex.getMessage());
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .bodyValue("Bad request: " + ex.getMessage())
                .flatMap(response -> response.writeTo(exchange, null));
    }
}