package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection.ReflectionStatus;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prompt_reflections", schema = "rutines")
@Getter
@Setter
@NoArgsConstructor
public class PromptReflectionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "prompt_text", nullable = false, length = 500)
    private String promptText;

    @Column(name = "answer", length = 2000)
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReflectionStatus status;
}