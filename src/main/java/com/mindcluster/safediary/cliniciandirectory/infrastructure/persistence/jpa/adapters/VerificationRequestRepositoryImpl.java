package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.VerificationRequest;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.VerificationRequestRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.VerificationRequestPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.VerificationRequestPersistenceRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.VerificationRequestPersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class VerificationRequestRepositoryImpl implements VerificationRequestRepository {
    public Optional<Long> findClinicianIdById(Long id) { return persistence.findClinicianIdById(id); }
    private final VerificationRequestPersistenceRepository persistence;
    private final DomainEventPublisher events;
    public Optional<VerificationRequest> findById(Long id) { return persistence.findById(id).map(VerificationRequestPersistenceAssembler::toDomain); }
    public Optional<VerificationRequest> findLatestByClinicianId(Long id) { return persistence.findFirstByClinicianIdOrderBySubmittedAtDescIdDesc(id).map(VerificationRequestPersistenceAssembler::toDomain); }
    public List<VerificationRequest> findPending() { return persistence.findByStatusOrderBySubmittedAtAsc(VerificationStatus.PENDING).stream().map(VerificationRequestPersistenceAssembler::toDomain).toList(); }
    @Transactional public VerificationRequest save(VerificationRequest value) {
        var entity = value.getId() == null ? new VerificationRequestPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        VerificationRequestPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(value);
        return VerificationRequestPersistenceAssembler.toDomain(saved);
    }
}
