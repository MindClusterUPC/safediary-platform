package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.domain.repositories.SosExerciseRepository;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.assemblers.SosExercisePersistenceAssembler;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.SosExercisePersistenceEntity;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.repositories.SosExercisePersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class SosExerciseRepositoryImpl implements SosExerciseRepository {

    private final SosExercisePersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public SosExerciseRepositoryImpl(SosExercisePersistenceRepository persistenceRepository,
                                     DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SosExercise> findById(Long id) {
        return persistenceRepository.findById(id).map(SosExercisePersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SosExercise> findAllByPatientId(Long patientId) {
        return persistenceRepository.findAllByPatientId(patientId).stream()
                .map(SosExercisePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SosExercise> findAll() {
        return persistenceRepository.findAll().stream()
                .map(SosExercisePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public SosExercise save(SosExercise exercise) {
        boolean isNew = exercise.getId() == null;
        SosExercisePersistenceEntity entity = isNew
                ? new SosExercisePersistenceEntity()
                : persistenceRepository.findById(exercise.getId())
                .orElseThrow(() -> new IllegalStateException("SosExercise not found: " + exercise.getId()));

        SosExercisePersistenceAssembler.copyToEntity(exercise, entity);
        SosExercisePersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        SosExercise result = SosExercisePersistenceAssembler.toDomain(saved);

        domainEventPublisher.publishAndClear(exercise);
        return result;
    }

    @Override
    @Transactional
    public void delete(SosExercise exercise) {
        if (exercise != null && exercise.getId() != null) {
            persistenceRepository.deleteById(exercise.getId());
        }
    }
}