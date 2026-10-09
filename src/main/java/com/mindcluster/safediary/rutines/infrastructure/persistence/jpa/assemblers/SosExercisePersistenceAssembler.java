package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.ExerciseType;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.StepMetrics;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.SosExercisePersistenceEntity;

public final class SosExercisePersistenceAssembler {

    private SosExercisePersistenceAssembler() {}

    public static SosExercise toDomain(SosExercisePersistenceEntity entity) {
        if (entity == null) return null;

        return new SosExercise(
                entity.getId(),
                entity.getPatientId(),
                ExerciseType.of(entity.getExerciseType()),
                StepMetrics.of(entity.getCurrentStep(), entity.getTotalSteps()),
                entity.getCompletedAt()
        );
    }

    public static void copyToEntity(SosExercise domain, SosExercisePersistenceEntity entity) {
        entity.setPatientId(domain.getPatientId());
        entity.setExerciseType(domain.getExerciseType().value());
        entity.setCurrentStep(domain.getStepMetrics().currentStep());
        entity.setTotalSteps(domain.getStepMetrics().totalSteps());
        entity.setCompletedAt(domain.getCompletedAt());
    }
}