package com.mindcluster.safediary.rutines.domain.model.queries;

public record GetPendingDailyReflectionQuery(Long userId) {
    public GetPendingDailyReflectionQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    public Long patientId() {
        return userId;
    }
}