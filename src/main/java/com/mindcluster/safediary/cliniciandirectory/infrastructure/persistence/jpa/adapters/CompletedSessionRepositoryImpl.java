package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.CompletedSession;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.CompletedSessionRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.CompletedSessionPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.CompletedSessionPersistenceRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.CompletedSessionPersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class CompletedSessionRepositoryImpl implements CompletedSessionRepository {
    private final CompletedSessionPersistenceRepository persistence;

    public Optional<CompletedSession> findByAppointmentId(Long id) { return persistence.findByAppointmentId(id).map(CompletedSessionPersistenceAssembler::toDomain); }
    public List<CompletedSession> findByClinicianId(Long id) { return persistence.findByClinicianId(id).stream().map(CompletedSessionPersistenceAssembler::toDomain).toList(); }
    @Transactional public CompletedSession save(CompletedSession value) {
        var entity = value.id() == null ? new CompletedSessionPersistenceEntity() : persistence.findById(value.id()).orElseThrow();
        CompletedSessionPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);

        return CompletedSessionPersistenceAssembler.toDomain(saved);
    }
}
