package com.company.bootcamp.task4.model.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Workout {
    private final UUID id;
    private final SportType type;
    private final int duration;
    private final double distance;
    private final String notes;
}