package com.mindcluster.safediary.rutines.domain.model.queries;

public record GetDailyRoutineByIdQuery(Long routineId) {
    public GetDailyRoutineByIdQuery {
        if (routineId == null) {
            throw new IllegalArgumentException("routineId is required");
        }
    }
}
