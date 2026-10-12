package com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/** Append-only: rows are inserted and never updated or deleted. */
@Entity
@Table(name="audit_events", schema="iam")
@Getter @Setter @NoArgsConstructor
public class AuditEventPersistenceEntity extends AuditableAbstractPersistenceEntity {
    private Long actorAccountId;
    private Long targetAccountId;
    private Long consentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=60)
    private AuditAction action;
    @Column(nullable=false, length=30)
    private String sourceContext;
    @Column(length=60)
    private String resourceType;
    private Long resourceId;
    @Column(nullable=false)
    private Instant occurredAt;
}
