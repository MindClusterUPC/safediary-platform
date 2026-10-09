package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.repositories.PatientRepository;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.assemblers.PatientPersistenceAssembler;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities.PatientPersistenceEntity;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.repositories.PatientPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PatientRepositoryImpl implements PatientRepository {

    private final PatientPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PatientRepositoryImpl(PatientPersistenceRepository persistenceRepository,
                                 DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findById(Long id) {
        return persistenceRepository.findById(id).map(PatientPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findByEmail(EmailAddress email) {
        return persistenceRepository.findByEmailIgnoreCase(email.value())
                .map(PatientPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return persistenceRepository.findAll().stream()
                .map(PatientPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Patient save(Patient patient) {
        boolean isNew = patient.getId() == null;
        PatientPersistenceEntity entity = isNew
                ? new PatientPersistenceEntity()
                : persistenceRepository.findById(patient.getId())
                .orElseThrow(() -> new IllegalStateException("Patient not found: " + patient.getId()));

        PatientPersistenceAssembler.copyToEntity(patient, entity);
        PatientPersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        Patient result = PatientPersistenceAssembler.toDomain(saved);

        domainEventPublisher.publishAndClear(patient);
        return result;
    }

    @Override
    @Transactional
    public void delete(Patient patient) {
        if (patient != null && patient.getId() != null) {
            persistenceRepository.deleteById(patient.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(EmailAddress email) {
        return persistenceRepository.existsByEmailIgnoreCase(email.value());
    }
}
