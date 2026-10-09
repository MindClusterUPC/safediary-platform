package com.mindcluster.safediary.rutines.interfaces.rest.transform;

import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.PromptReflectionResponseResource;

public final class PromptReflectionResourceAssembler {

    private PromptReflectionResourceAssembler() {}

    public static PromptReflectionResponseResource toResource(PromptReflection reflection) {
        if (reflection == null) return null;

        String answerValue = reflection.getAnswer() != null 
                ? reflection.getAnswer().value() 
                : null;

        return new PromptReflectionResponseResource(
                reflection.getId(),
                reflection.getPatientId(),
                reflection.getPromptText().value(),
                answerValue,
                reflection.getStatus().name()
        );
    }
}