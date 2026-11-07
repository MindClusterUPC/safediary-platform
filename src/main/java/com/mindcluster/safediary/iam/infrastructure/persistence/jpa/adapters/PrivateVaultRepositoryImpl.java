package com.mindcluster.safediary.iam.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.iam.domain.model.aggregates.PrivateVault;
import com.mindcluster.safediary.iam.domain.repositories.PrivateVaultRepository;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.assemblers.IamPersistenceAssembler;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities.PrivateVaultPersistenceEntity;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.repositories.PrivateVaultPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class PrivateVaultRepositoryImpl implements PrivateVaultRepository {
    private final PrivateVaultPersistenceRepository persistence;
    private final DomainEventPublisher events;

    public Optional<PrivateVault> findByAccountId(Long accountId) {
        return persistence.findByAccountId(accountId).map(IamPersistenceAssembler::toDomain);
    }

    @Transactional public PrivateVault save(PrivateVault vault) {
        var entity = vault.getId() == null ? new PrivateVaultPersistenceEntity() : persistence.findById(vault.getId()).orElseThrow();
        IamPersistenceAssembler.copyToEntity(vault, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(vault);
        return IamPersistenceAssembler.toDomain(saved);
    }
}
