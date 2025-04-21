package com.company.bootcamp.task4.services.plugin;

import java.util.*;

import com.company.bootcamp.task4.model.plugin.DeleteWorkoutResponse;
import com.company.bootcamp.task4.model.plugin.SportType;
import com.company.bootcamp.task4.model.plugin.Workout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WorkoutService {

    private final Map<UUID, List<Workout>> userWorkouts = new HashMap<>();
    public static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public Mono<Workout> addWorkout(SportType type, int duration, double distance, String notes) {
        Workout workout = new Workout(UUID.randomUUID(), type, duration, distance, notes);
        userWorkouts.computeIfAbsent(USER_ID, k -> new ArrayList<>()).add(workout);
        return Mono.just(workout);
    }

    public Mono<Workout> getWorkoutDetails(UUID workoutId) {
        List<Workout> workouts = userWorkouts.get(USER_ID);
        if (workouts == null) {
            return Mono.empty();
        }
        return Mono.justOrEmpty(workouts.stream()
                .filter(workout -> workout.getId().equals(workoutId))
                .findFirst());
    }

    public Mono<DeleteWorkoutResponse> deleteWorkout(UUID workoutId) {
        List<Workout> workouts = userWorkouts.get(USER_ID);
        if (workouts == null) {
            return Mono.just(new DeleteWorkoutResponse(false, "User has no workouts"));
        }
        boolean removed = workouts.removeIf(workout -> workout.getId().equals(workoutId));
        if (removed) {
            return Mono.just(new DeleteWorkoutResponse(true, "Workout deleted successfully"));
        } else {
            return Mono.just(new DeleteWorkoutResponse(false, "Workout not found"));
        }
    }

    public Mono<List<Workout>> getAllWorkouts() {
        List<Workout> workouts = userWorkouts.getOrDefault(USER_ID, Collections.emptyList());
        return Mono.just(workouts);
    }
}