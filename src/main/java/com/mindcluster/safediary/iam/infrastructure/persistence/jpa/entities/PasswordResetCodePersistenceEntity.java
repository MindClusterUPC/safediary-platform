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

/** Stored in password_reset_tokens: one row per 6-digit recovery code, hashed with BCrypt. */
@Entity
@Table(name="password_reset_tokens", schema="iam")
@Getter @Setter @NoArgsConstructor
public class PasswordResetCodePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long accountId;
    @Column(name="token_hash", nullable=false, length=100)
    private String codeHash;
    @Column(nullable=false)
    private Instant expiresAt;
    private Instant usedAt;
    @Column(nullable=false)
    private int failedAttempts;
}
