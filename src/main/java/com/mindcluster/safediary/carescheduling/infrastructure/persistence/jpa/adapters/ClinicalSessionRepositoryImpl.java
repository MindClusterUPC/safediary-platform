package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.ClinicalSession;
import com.mindcluster.safediary.carescheduling.domain.repositories.ClinicalSessionRepository;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.assemblers.CareSchedulingPersistenceAssembler;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.ClinicalSessionPersistenceEntity;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories.ClinicalSessionPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class ClinicalSessionRepositoryImpl implements ClinicalSessionRepository {
    private final ClinicalSessionPersistenceRepository persistence;
    private final DomainEventPublisher events;

    public Optional<ClinicalSession> findByAppointmentId(Long id) {
        return persistence.findByAppointmentId(id).map(CareSchedulingPersistenceAssembler::toDomain);
    }

    @Transactional public ClinicalSession save(ClinicalSession value) {
        var entity = value.getId() == null ? new ClinicalSessionPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        CareSchedulingPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(value);
        return CareSchedulingPersistenceAssembler.toDomain(saved);
    }
}
