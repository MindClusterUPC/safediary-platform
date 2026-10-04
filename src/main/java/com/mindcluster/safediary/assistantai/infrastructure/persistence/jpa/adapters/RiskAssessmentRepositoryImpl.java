package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;
import com.mindcluster.safediary.assistantai.domain.repositories.RiskAssessmentRepository;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.assemblers.RiskAssessmentPersistenceAssembler;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories.RiskAssessmentPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository adapter that bridges the risk assessment repository port with Spring Data JPA.
 */
@Repository
public class RiskAssessmentRepositoryImpl implements RiskAssessmentRepository {

    private final RiskAssessmentPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public RiskAssessmentRepositoryImpl(RiskAssessmentPersistenceRepository persistenceRepository,
                                        DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public RiskAssessment save(RiskAssessment assessment) {
        var saved = persistenceRepository.saveAndFlush(RiskAssessmentPersistenceAssembler.toPersistenceFromDomain(assessment));
        domainEventPublisher.publishAndClear(assessment);
        return RiskAssessmentPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RiskAssessment> findLatestBySessionId(Long sessionId) {
        return persistenceRepository.findFirstBySessionIdOrderByAssessedAtDesc(sessionId)
                .map(RiskAssessmentPersistenceAssembler::toDomainFromPersistence);
    }
}
