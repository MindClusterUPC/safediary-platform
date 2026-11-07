package com.mindcluster.safediary.iam.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.iam.domain.model.aggregates.Consent;
import com.mindcluster.safediary.iam.domain.model.valueobjects.ConsentStatus;
import com.mindcluster.safediary.iam.domain.repositories.ConsentRepository;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.assemblers.IamPersistenceAssembler;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities.ConsentPersistenceEntity;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.repositories.ConsentPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class ConsentRepositoryImpl implements ConsentRepository {
    private final ConsentPersistenceRepository persistence;
    private final DomainEventPublisher events;

    public Optional<Consent> findById(Long id) { return persistence.findById(id).map(IamPersistenceAssembler::toDomain); }
    public List<Consent> findByOwnerAccountId(Long id) { return persistence.findByOwnerAccountId(id).stream().map(IamPersistenceAssembler::toDomain).toList(); }
    public List<Consent> findByGranteeAccountId(Long id) { return persistence.findByGranteeAccountId(id).stream().map(IamPersistenceAssembler::toDomain).toList(); }
    public List<Consent> findActiveInvolving(Long accountId) {
        return persistence.findByStatusInvolving(ConsentStatus.ACTIVE, accountId).stream().map(IamPersistenceAssembler::toDomain).toList();
    }

    @Transactional public Consent save(Consent consent) {
        var entity = consent.getId() == null ? new ConsentPersistenceEntity() : persistence.findById(consent.getId()).orElseThrow();
        IamPersistenceAssembler.copyToEntity(consent, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(consent);
        return IamPersistenceAssembler.toDomain(saved);
    }
}
