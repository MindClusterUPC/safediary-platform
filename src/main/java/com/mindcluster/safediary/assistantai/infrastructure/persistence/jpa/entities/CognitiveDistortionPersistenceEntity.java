package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for cognitive distortions detected in user messages.
 */
@Entity
@Table(name = "cognitive_distortions")
@Getter
@Setter
@NoArgsConstructor
public class CognitiveDistortionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id")
    private ConversationMessagePersistenceEntity message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DistortionType type;

    @Column(length = 300)
    private String evidence;

    private double confidence;
}
