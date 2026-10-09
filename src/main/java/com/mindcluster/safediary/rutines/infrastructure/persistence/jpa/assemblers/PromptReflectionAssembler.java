package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.PromptText;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.ReflectionAnswer;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.PromptReflectionPersistenceEntity;

public final class PromptReflectionPersistenceAssembler {

    private PromptReflectionPersistenceAssembler() {}

    public static PromptReflection toDomain(PromptReflectionPersistenceEntity entity) {
        if (entity == null) return null;

        // La respuesta puede ser nula si la reflexión sigue en estado PENDING
        ReflectionAnswer answerVo = entity.getAnswer() != null 
                ? ReflectionAnswer.of(entity.getAnswer()) 
                : null;

        return new PromptReflection(
                entity.getId(),
                entity.getPatientId(),
                PromptText.of(entity.getPromptText()),
                answerVo,
                entity.getStatus()
        );
    }

    public static void copyToEntity(PromptReflection domain, PromptReflectionPersistenceEntity entity) {
        entity.setPatientId(domain.getPatientId());
        entity.setPromptText(domain.getPromptText().value()); 
        
        entity.setAnswer(
                domain.getAnswer() != null ? domain.getAnswer().value() : null
        );
        
        entity.setStatus(domain.getStatus());
    }
}