package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers.SubscriptionPersistenceAssembler;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories.SubscriptionPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final SubscriptionPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public SubscriptionRepositoryImpl(SubscriptionPersistenceRepository persistenceRepository,
                                      DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findById(Long id) {
        return persistenceRepository.findById(id).map(SubscriptionPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findByPatientAccountId(Long patientAccountId) {
        return persistenceRepository.findByPatientAccountId(patientAccountId)
                .map(SubscriptionPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientAccountId(Long patientAccountId) {
        return persistenceRepository.existsByPatientAccountId(patientAccountId);
    }

    @Override
    @Transactional
    public Subscription save(Subscription subscription) {
        boolean isNew = subscription.getId() == null;
        SubscriptionPersistenceEntity entity = isNew
                ? persistenceRepository.findByPatientAccountId(subscription.getPatientAccountId()).orElseGet(SubscriptionPersistenceEntity::new)
                : persistenceRepository.findById(subscription.getId())
                .orElseGet(SubscriptionPersistenceEntity::new);

        SubscriptionPersistenceAssembler.copyToEntity(subscription, entity);
        SubscriptionPersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        Subscription result = SubscriptionPersistenceAssembler.toDomain(saved);

        domainEventPublisher.publishAndClear(subscription);
        return result;
    }
}
