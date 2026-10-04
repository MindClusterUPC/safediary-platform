package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA persistence entity for append-only risk assessments.
 */
@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
public class RiskAssessmentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long sessionId;

    private double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RiskLevel riskLevel;

    @Column(length = 500)
    private String triggerKeywords;

    private boolean crisisProtocolActivated;

    @Column(nullable = false)
    private Instant assessedAt;
}
