package com.mindcluster.safediary.rutines.domain.model.queries;

public record GetAllRoutinesByUserIdQuery(Long userId) {
    public GetAllRoutinesByUserIdQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    public Long patientId() {
        return userId;
    }
}