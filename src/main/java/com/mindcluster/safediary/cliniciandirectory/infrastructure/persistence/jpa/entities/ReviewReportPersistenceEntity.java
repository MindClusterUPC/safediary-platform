package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="review_reports", schema="clinician_directory")
@Getter @Setter @NoArgsConstructor
public class ReviewReportPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long reviewId;
    @Column(nullable=false)
    private Long reporterAccountId;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private ReportReason reason;
    @Column(length=1000)
    private String comment;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ReportStatus status;
    @Column(nullable=false)
    private Instant reportedAt;
    @Column()
    private Long resolvedByAccountId;
    @Column()
    private Instant resolvedAt;
    @Column(length=1000)
    private String resolution;

}
