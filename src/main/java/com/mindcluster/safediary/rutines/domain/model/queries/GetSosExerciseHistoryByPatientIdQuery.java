package com.mindcluster.safediary.rutines.domain.model.queries;

public record GetSosExerciseHistoryByPatientIdQuery(Long patientId) {
    public GetSosExerciseHistoryByPatientIdQuery {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
    }
}
