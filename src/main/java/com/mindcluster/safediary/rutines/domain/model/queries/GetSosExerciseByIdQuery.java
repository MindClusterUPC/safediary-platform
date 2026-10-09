package com.mindcluster.safediary.rutines.domain.model.queries;

public record GetSosExerciseByIdQuery(Long sosExerciseId) {
    public GetSosExerciseByIdQuery {
        if (sosExerciseId == null) {
            throw new IllegalArgumentException("sosExerciseId is required");
        }
    }
}
