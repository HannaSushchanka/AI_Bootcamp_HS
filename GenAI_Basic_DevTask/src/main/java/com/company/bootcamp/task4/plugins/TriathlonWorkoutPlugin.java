package com.company.bootcamp.task4.plugins;

import com.company.bootcamp.task4.model.plugin.DeleteWorkoutResponse;
import com.company.bootcamp.task4.model.plugin.SportType;
import com.company.bootcamp.task4.model.plugin.Workout;
import com.company.bootcamp.task4.model.plugin.Workouts;
import com.company.bootcamp.task4.services.plugin.WorkoutService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Component
public class TriathlonWorkoutPlugin {

    private WorkoutService workoutService;

    @DefineKernelFunction(name = "add_workout", description = "Add a workout to the user's schedule.", returnType = "com.company.bootcamp.task4.model.plugin.Workout")
    public Mono<Workout> addWorkout(SportType type, int duration, double distance, String notes) {
        return workoutService.addWorkout(type, duration, distance, notes);
    }

    @DefineKernelFunction(name = "get_workout_details", description = "Get details about a specific workout.", returnType = "com.company.bootcamp.task4.model.plugin.Workout")
    public Mono<Workout> getWorkoutDetails(UUID workoutId) {
        return workoutService.getWorkoutDetails(workoutId);
    }

    @DefineKernelFunction(name = "delete_workout", description = "Delete a workout from the user's schedule.", returnType = "com.company.bootcamp.task4.model.plugin.DeleteWorkoutResponse")
    public Mono<DeleteWorkoutResponse> deleteWorkout(UUID workoutId) {
        return workoutService.deleteWorkout(workoutId);
    }

    @DefineKernelFunction(
            name = "get_all_workouts",
            description = "Get all workouts in the user's schedule.",
            returnType = "com.company.bootcamp.task4.model.plugin.Workouts"
    )
    public Mono<Workouts> getAllWorkouts() {
        return workoutService.getAllWorkouts()
                .map(Workouts::new);
    }
}