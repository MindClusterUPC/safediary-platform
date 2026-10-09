package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.TrustScore;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.TrustScoreRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.TrustScorePersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.TrustScorePersistenceRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.TrustScorePersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class TrustScoreRepositoryImpl implements TrustScoreRepository {
    private final TrustScorePersistenceRepository persistence;

    public Optional<TrustScore> findByClinicianId(Long id) { return persistence.findByClinicianId(id).map(TrustScorePersistenceAssembler::toDomain); }
    @Transactional public TrustScore save(TrustScore value) {
        var entity = persistence.findByClinicianId(value.clinicianId()).orElseGet(TrustScorePersistenceEntity::new);
        TrustScorePersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);

        return TrustScorePersistenceAssembler.toDomain(saved);
    }
}
