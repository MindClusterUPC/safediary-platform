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
@Table(name="auth_providers", schema="iam",
        uniqueConstraints=@UniqueConstraint(name="ux_auth_providers_external", columnNames={"provider_type", "external_id"}))
@Getter @Setter @NoArgsConstructor
public class AuthProviderPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long accountId;
    @Enumerated(EnumType.STRING)
    @Column(name="provider_type", nullable=false, length=20)
    private AuthProviderType providerType;
    @Column(name="external_id", nullable=false, length=255)
    private String externalId;
}
