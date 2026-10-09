package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.Appointment;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.AppointmentStatus;
import com.mindcluster.safediary.carescheduling.domain.repositories.AppointmentRepository;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.assemblers.CareSchedulingPersistenceAssembler;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories.*;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Persists the Appointment aggregate together with its SlotHold, which lives in its own table. */
@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class AppointmentRepositoryImpl implements AppointmentRepository {
    private final AppointmentPersistenceRepository persistence;
    private final SlotHoldPersistenceRepository holds;
    private final DomainEventPublisher events;

    private Appointment toDomain(AppointmentPersistenceEntity entity) {
        return CareSchedulingPersistenceAssembler.toDomain(entity, holds.findByAppointmentId(entity.getId()).orElse(null));
    }
    private List<Appointment> toDomain(List<AppointmentPersistenceEntity> entities) {
        if (entities.isEmpty()) return List.of();
        var byAppointment = holds.findByAppointmentIdIn(entities.stream().map(AppointmentPersistenceEntity::getId).toList())
                .stream().collect(Collectors.toMap(SlotHoldPersistenceEntity::getAppointmentId, Function.identity()));
        return entities.stream().map(e -> CareSchedulingPersistenceAssembler.toDomain(e, byAppointment.get(e.getId()))).toList();
    }

    public Optional<Appointment> findById(Long id) { return persistence.findById(id).map(this::toDomain); }
    @Transactional public Optional<Appointment> findByIdForUpdate(Long id) { return persistence.findByIdForUpdate(id).map(this::toDomain); }
    public List<Appointment> findByPatientAccountId(Long id) { return toDomain(persistence.findByPatientAccountId(id)); }
    public List<Appointment> findByClinicianId(Long id) { return toDomain(persistence.findByClinicianId(id)); }
    public List<Appointment> findByClinicianIdOverlapping(Long clinicianId, Instant from, Instant to) {
        return toDomain(persistence.findByClinicianIdOverlapping(clinicianId, from, to));
    }
    public List<Appointment> findByContactRequestIdAndStatus(Long contactRequestId, AppointmentStatus status) {
        return toDomain(persistence.findByContactRequestIdAndStatus(contactRequestId, status));
    }
    public List<Appointment> findHeldWithHoldExpiredAt(Instant now) { return toDomain(persistence.findHeldWithHoldExpiredAt(now)); }

    @Transactional public Appointment save(Appointment value) {
        var entity = value.getId() == null ? new AppointmentPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        CareSchedulingPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);
        if (value.getHold() != null) {
            var hold = holds.findByAppointmentId(saved.getId()).orElseGet(SlotHoldPersistenceEntity::new);
            CareSchedulingPersistenceAssembler.copyToEntity(saved.getId(), value.getHold(), hold);
            holds.saveAndFlush(hold);
        }
        events.publishAndClear(value);
        return toDomain(saved);
    }
}
