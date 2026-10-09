package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.carescheduling.domain.model.entities.AvailabilitySlot;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.AvailabilityWindow;
import com.mindcluster.safediary.carescheduling.domain.repositories.AvailabilitySlotRepository;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.assemblers.CareSchedulingPersistenceAssembler;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.AvailabilitySlotPersistenceEntity;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories.AvailabilitySlotPersistenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class AvailabilitySlotRepositoryImpl implements AvailabilitySlotRepository {
    private final AvailabilitySlotPersistenceRepository persistence;

    public List<AvailabilitySlot> findActiveByClinicianId(Long id) {
        return persistence.findByClinicianIdAndActiveTrue(id).stream().map(CareSchedulingPersistenceAssembler::toDomain).toList();
    }
    @Transactional public List<AvailabilitySlot> lockActiveByClinicianId(Long id) {
        return persistence.lockActiveByClinicianId(id).stream().map(CareSchedulingPersistenceAssembler::toDomain).toList();
    }

    /** Previous windows are deactivated instead of deleted, so the agenda history stays auditable. */
    @Transactional public List<AvailabilitySlot> replaceActive(Long clinicianId, List<AvailabilityWindow> windows) {
        var previous = persistence.findByClinicianIdAndActiveTrue(clinicianId);
        previous.forEach(slot -> slot.setActive(false));
        persistence.saveAllAndFlush(previous);
        var created = windows.stream().map(window -> {
            var entity = new AvailabilitySlotPersistenceEntity();
            CareSchedulingPersistenceAssembler.copyToEntity(new AvailabilitySlot(null, clinicianId, window, true), entity);
            return entity;
        }).toList();
        return persistence.saveAllAndFlush(created).stream().map(CareSchedulingPersistenceAssembler::toDomain).toList();
    }
}
