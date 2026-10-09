package com.mindcluster.safediary.rutines.interfaces.rest.transform;

import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.SosExerciseResponseResource;

public final class SosExerciseResourceAssembler {

    private SosExerciseResourceAssembler() {}

    public static SosExerciseResponseResource toResource(SosExercise exercise) {
        if (exercise == null) return null;

        return new SosExerciseResponseResource(
                exercise.getId(),
                exercise.getPatientId(),
                exercise.getExerciseType().value(),
                exercise.getStepMetrics().currentStep(),
                exercise.getStepMetrics().totalSteps(),
                exercise.getCompletedAt()
        );
    }
}