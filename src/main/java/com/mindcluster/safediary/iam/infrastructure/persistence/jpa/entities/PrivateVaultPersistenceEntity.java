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
@Table(name="private_vaults", schema="iam")
@Getter @Setter @NoArgsConstructor
public class PrivateVaultPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false, unique=true)
    private Long accountId;
    @Column(nullable=false, length=100)
    private String pinHash;
    @Column(nullable=false)
    private int failedAttempts;
    private Instant lockedUntil;

    /** References to diary entries; the content stays in Diary. */
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="vault_entries", schema="iam", joinColumns=@JoinColumn(name="vault_id"))
    @Column(name="diary_entry_id", nullable=false)
    private Set<Long> diaryEntryIds = new LinkedHashSet<>();
}
