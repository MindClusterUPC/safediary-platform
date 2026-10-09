package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.repositories.PsychologistRepository;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.assemblers.PsychologistPersistenceAssembler;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities.PsychologistPersistenceEntity;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.repositories.PsychologistPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PsychologistRepositoryImpl implements PsychologistRepository {

    private final PsychologistPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PsychologistRepositoryImpl(PsychologistPersistenceRepository persistenceRepository,
                                      DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Psychologist> findById(Long id) {
        return persistenceRepository.findById(id).map(PsychologistPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Psychologist> findByEmail(EmailAddress email) {
        return persistenceRepository.findByEmailIgnoreCase(email.value())
                .map(PsychologistPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Psychologist> findAll() {
        return persistenceRepository.findAll().stream()
                .map(PsychologistPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Psychologist save(Psychologist psychologist) {
        boolean isNew = psychologist.getId() == null;
        PsychologistPersistenceEntity entity = isNew
                ? new PsychologistPersistenceEntity()
                : persistenceRepository.findById(psychologist.getId())
                .orElseThrow(() -> new IllegalStateException("Psychologist not found: " + psychologist.getId()));

        PsychologistPersistenceAssembler.copyToEntity(psychologist, entity);
        PsychologistPersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        Psychologist result = PsychologistPersistenceAssembler.toDomain(saved);

        domainEventPublisher.publishAndClear(psychologist);
        return result;
    }

    @Override
    @Transactional
    public void delete(Psychologist psychologist) {
        if (psychologist != null && psychologist.getId() != null) {
            persistenceRepository.deleteById(psychologist.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(EmailAddress email) {
        return persistenceRepository.existsByEmailIgnoreCase(email.value());
    }
}
