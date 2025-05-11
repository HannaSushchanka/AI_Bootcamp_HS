package com.company.bootcamp.task5.configuration.exceptions;

import lombok.Getter;
import com.company.bootcamp.task5.model.ErrorResponse;

@Getter
public class ServiceException extends Exception {
    private final ErrorResponse errorResponse;

    public ServiceException(String message, int status) {
        super(message);
        this.errorResponse = new ErrorResponse(status, message);
    }

    public ServiceException(String message, Throwable cause, int status) {
        super(message, cause);
        this.errorResponse = new ErrorResponse(status, message);
    }
}
