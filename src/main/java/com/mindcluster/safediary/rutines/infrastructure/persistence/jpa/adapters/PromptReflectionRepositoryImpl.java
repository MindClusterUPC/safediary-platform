package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection.ReflectionStatus;
import com.mindcluster.safediary.rutines.domain.repositories.PromptReflectionRepository;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.assemblers.PromptReflectionPersistenceAssembler;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.PromptReflectionPersistenceEntity;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.repositories.PromptReflectionPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PromptReflectionRepositoryImpl implements PromptReflectionRepository {

    private final PromptReflectionPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PromptReflectionRepositoryImpl(PromptReflectionPersistenceRepository persistenceRepository,
                                          DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromptReflection> findById(Long id) {
        return persistenceRepository.findById(id).map(PromptReflectionPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromptReflection> findByPatientIdAndStatus(Long patientId, ReflectionStatus status) {
        return persistenceRepository.findByPatientIdAndStatus(patientId, status)
                .map(PromptReflectionPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromptReflection> findAll() {
        return persistenceRepository.findAll().stream()
                .map(PromptReflectionPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public PromptReflection save(PromptReflection reflection) {
        boolean isNew = reflection.getId() == null;
        PromptReflectionPersistenceEntity entity = isNew
                ? new PromptReflectionPersistenceEntity()
                : persistenceRepository.findById(reflection.getId())
                .orElseThrow(() -> new IllegalStateException("PromptReflection not found: " + reflection.getId()));

        PromptReflectionPersistenceAssembler.copyToEntity(reflection, entity);
        PromptReflectionPersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        PromptReflection result = PromptReflectionPersistenceAssembler.toDomain(saved);

        domainEventPublisher.publishAndClear(reflection);
        return result;
    }

    @Override
    @Transactional
    public void delete(PromptReflection reflection) {
        if (reflection != null && reflection.getId() != null) {
            persistenceRepository.deleteById(reflection.getId());
        }
    }
}