package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * JPA persistence entity for weekly clinical summaries.
 */
@Entity
@Table(name = "clinical_summaries")
@Getter
@Setter
@NoArgsConstructor
public class ClinicalSummaryPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(length = 200)
    private String dominantEmotions;

    @Column(columnDefinition = "TEXT")
    private String keyTriggers;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String synthesisNarrative;

    @Column(columnDefinition = "TEXT")
    private String highlights;

    @Column(nullable = false)
    private Instant generatedAt;
}
