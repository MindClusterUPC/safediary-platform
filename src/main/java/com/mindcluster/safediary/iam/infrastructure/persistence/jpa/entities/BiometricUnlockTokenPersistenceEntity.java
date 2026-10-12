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

/** Stored in refresh_tokens: long-lived device credential for biometric quick unlock (SHA-256 hash only). */
@Entity
@Table(name="refresh_tokens", schema="iam")
@Getter @Setter @NoArgsConstructor
public class BiometricUnlockTokenPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long accountId;
    @Column(nullable=false, unique=true, length=64)
    private String tokenHash;
    @Column(nullable=false)
    private Instant expiresAt;
    private Instant revokedAt;
}
