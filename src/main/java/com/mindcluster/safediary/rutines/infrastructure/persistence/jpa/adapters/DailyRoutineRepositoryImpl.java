package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.repositories.DailyRoutineRepository;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.assemblers.DailyRoutinePersistenceAssembler;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.DailyRoutinePersistenceEntity;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.repositories.DailyRoutinePersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class DailyRoutineRepositoryImpl implements DailyRoutineRepository {

    private final DailyRoutinePersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public DailyRoutineRepositoryImpl(DailyRoutinePersistenceRepository persistenceRepository,
                                      DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DailyRoutine> findById(Long id) {
        return persistenceRepository.findById(id).map(DailyRoutinePersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyRoutine> findAllByPatientId(Long patientId) {
        return persistenceRepository.findAllByPatientId(patientId).stream()
                .map(DailyRoutinePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyRoutine> findAll() {
        return persistenceRepository.findAll().stream()
                .map(DailyRoutinePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public DailyRoutine save(DailyRoutine routine) {
        boolean isNew = routine.getId() == null;
        DailyRoutinePersistenceEntity entity = isNew
                ? new DailyRoutinePersistenceEntity()
                : persistenceRepository.findById(routine.getId())
                .orElseThrow(() -> new IllegalStateException("DailyRoutine not found: " + routine.getId()));

        DailyRoutinePersistenceAssembler.copyToEntity(routine, entity);
        DailyRoutinePersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        DailyRoutine result = DailyRoutinePersistenceAssembler.toDomain(saved);

        domainEventPublisher.publishAndClear(routine);
        return result;
    }

    @Override
    @Transactional
    public void delete(DailyRoutine routine) {
        if (routine != null && routine.getId() != null) {
            persistenceRepository.deleteById(routine.getId());
        }
    }
}