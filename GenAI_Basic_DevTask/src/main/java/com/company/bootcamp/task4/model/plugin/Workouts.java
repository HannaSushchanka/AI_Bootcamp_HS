package com.company.bootcamp.task4.model.plugin;

import java.util.List;

public class Workouts {
    private List<Workout> workouts;

    public Workouts(List<Workout> workouts) {
        this.workouts = workouts;
    }

    public List<Workout> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
    }

}
