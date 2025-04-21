package com.company.bootcamp.task4.model.plugin;

public class DeleteWorkoutResponse {
    private final boolean success;
    private final String message;

    public DeleteWorkoutResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
