package com.company.bootcamp.task4.model.plugin;

public record AddWorkoutRequest(SportType type, int duration, double distance, String notes) {}
