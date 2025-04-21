package com.company.bootcamp.task4.controllers;

import com.company.bootcamp.task4.model.plugin.AddWorkoutRequest;
import com.company.bootcamp.task4.model.plugin.DeleteWorkoutResponse;
import com.company.bootcamp.task4.model.plugin.Workout;
import com.company.bootcamp.task4.plugins.TriathlonWorkoutPlugin;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workouts")
@AllArgsConstructor
public class TriathlonWorkoutController {

    private final TriathlonWorkoutPlugin triathlonWorkoutPlugin;

    @PostMapping
    public Mono<ResponseEntity<Workout>> addWorkout(@RequestBody AddWorkoutRequest request) {
        return triathlonWorkoutPlugin.addWorkout(
                request.type(),
                request.duration(),
                request.distance(),
                request.notes()
        ).map(ResponseEntity::ok);
    }

    @GetMapping("/{workoutId}")
    public Mono<ResponseEntity<Workout>> getWorkoutDetails(@PathVariable UUID workoutId) {
        return triathlonWorkoutPlugin.getWorkoutDetails(workoutId)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{workoutId}")
    public Mono<ResponseEntity<DeleteWorkoutResponse>> deleteWorkout(@PathVariable UUID workoutId) {
        return triathlonWorkoutPlugin.deleteWorkout(workoutId)
                .map(ResponseEntity::ok);
    }

}