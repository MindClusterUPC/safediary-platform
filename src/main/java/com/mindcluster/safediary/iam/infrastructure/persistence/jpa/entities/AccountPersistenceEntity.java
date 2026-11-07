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
@Table(name="accounts", schema="iam")
@Getter @Setter @NoArgsConstructor
public class AccountPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false, unique=true, length=254)
    private String email;
    @Column(length=100)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private AccountRole role;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private AccountStatus status;
    @Column(nullable=false)
    private boolean biometricEnabled;
    @Column(name="failed_login_attempts", nullable=false)
    private int failedSignInAttempts;
    private Instant lockedUntil;
    private Instant deletionScheduledAt;
}
