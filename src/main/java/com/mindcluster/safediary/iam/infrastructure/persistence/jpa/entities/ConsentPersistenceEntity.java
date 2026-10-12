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

@Entity
@Table(name="consents", schema="iam")
@Getter @Setter @NoArgsConstructor
public class ConsentPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="patient_account_id", nullable=false)
    private Long ownerAccountId;
    @Column(name="specialist_account_id", nullable=false)
    private Long granteeAccountId;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ConsentStatus status;
    @Column(nullable=false)
    private Instant grantedAt;
    private Instant expiresAt;
    private Instant revokedAt;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="sharing_permissions", schema="iam", joinColumns=@JoinColumn(name="consent_id"))
    private List<SharingPermissionEmbeddable> permissions = new ArrayList<>();

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    public static class SharingPermissionEmbeddable {
        @Enumerated(EnumType.STRING)
        @Column(nullable=false, length=30)
        private SharingScope scope;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }
}
