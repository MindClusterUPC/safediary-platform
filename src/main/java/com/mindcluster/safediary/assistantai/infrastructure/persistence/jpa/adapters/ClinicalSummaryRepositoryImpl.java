package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.domain.repositories.ClinicalSummaryRepository;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.assemblers.ClinicalSummaryPersistenceAssembler;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories.ClinicalSummaryPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository adapter that bridges the clinical summary repository port with Spring Data JPA.
 */
@Repository
public class ClinicalSummaryRepositoryImpl implements ClinicalSummaryRepository {

    private final ClinicalSummaryPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ClinicalSummaryRepositoryImpl(ClinicalSummaryPersistenceRepository persistenceRepository,
                                         DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public ClinicalSummary save(ClinicalSummary summary) {
        var saved = persistenceRepository.saveAndFlush(ClinicalSummaryPersistenceAssembler.toPersistenceFromDomain(summary));
        domainEventPublisher.publishAndClear(summary);
        return ClinicalSummaryPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClinicalSummary> findByAccountIdAndPeriodStart(Long accountId, LocalDate periodStart) {
        return persistenceRepository.findFirstByAccountIdAndPeriodStart(accountId, periodStart)
                .map(ClinicalSummaryPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClinicalSummary> findLatestByAccountId(Long accountId) {
        return persistenceRepository.findFirstByAccountIdOrderByPeriodStartDesc(accountId)
                .map(ClinicalSummaryPersistenceAssembler::toDomainFromPersistence);
    }
}
