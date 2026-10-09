package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.ClinicianProfile;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.ClinicianProfileRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.ClinicianProfilePersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class ClinicianProfileRepositoryImpl implements ClinicianProfileRepository {
    private final ClinicianProfilePersistenceRepository persistence;
    private final ConsultationRatePersistenceRepository rates;
    private final DomainEventPublisher events;

    private ClinicianProfile toDomain(ClinicianProfilePersistenceEntity entity) {
        var rate = rates.findFirstByClinicianIdOrderByVersionDesc(entity.getId()).orElseThrow();
        return ClinicianProfilePersistenceAssembler.toDomain(entity,
                new ConsultationRate(rate.getAmount(), rate.getCurrency(), rate.getDurationMinutes(), rate.getVersion()));
    }
    public Optional<ClinicianProfile> findById(Long id) { return persistence.findById(id).map(this::toDomain); }
    @Transactional public Optional<ClinicianProfile> findByIdForUpdate(Long id) { return persistence.findByIdForUpdate(id).map(this::toDomain); }
    public Optional<ClinicianProfile> findByAccountId(Long accountId) { return persistence.findByAccountId(accountId).map(this::toDomain); }
    public List<ClinicianProfile> findPublicProfiles() {
        return persistence.findByVerificationStatusAndPublicationStatus(VerificationStatus.APPROVED, PublicationStatus.PUBLISHED)
                .stream().map(this::toDomain).toList();
    }

    @Transactional public ClinicianProfile save(ClinicianProfile profile) {
        var entity = profile.getId() == null ? new ClinicianProfilePersistenceEntity()
                : persistence.findById(profile.getId()).orElseThrow();
        ClinicianProfilePersistenceAssembler.copyToEntity(profile, entity);
        entity = persistence.saveAndFlush(entity);
        var latest = rates.findFirstByClinicianIdOrderByVersionDesc(entity.getId());
        if (latest.isEmpty() || latest.get().getVersion() != profile.getRate().version()) {
            var active = rates.findByClinicianIdAndActiveTrue(entity.getId());
            active.forEach(r -> r.setActive(false));
            rates.saveAllAndFlush(active);
            var rate = new ConsultationRatePersistenceEntity();
            rate.setClinicianId(entity.getId()); rate.setAmount(profile.getRate().amount());
            rate.setCurrency(profile.getRate().currency()); rate.setDurationMinutes(profile.getRate().durationMinutes());
            rate.setVersion(profile.getRate().version()); rate.setEffectiveAt(Instant.now()); rate.setActive(true);
            rates.saveAndFlush(rate);
        }
        events.publishAndClear(profile);
        return toDomain(entity);
    }
}
