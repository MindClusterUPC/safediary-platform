package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.RatingSummary;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.RatingSummaryRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.RatingSummaryPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.RatingSummaryPersistenceRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.RatingSummaryPersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class RatingSummaryRepositoryImpl implements RatingSummaryRepository {
    private final RatingSummaryPersistenceRepository persistence;

    public Optional<RatingSummary> findByClinicianId(Long id) { return persistence.findByClinicianId(id).map(RatingSummaryPersistenceAssembler::toDomain); }
    @Transactional public RatingSummary save(RatingSummary value) {
        var entity = persistence.findByClinicianId(value.clinicianId()).orElseGet(RatingSummaryPersistenceEntity::new);
        RatingSummaryPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);

        return RatingSummaryPersistenceAssembler.toDomain(saved);
    }
}
