package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineTitle;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.DailyRoutinePersistenceEntity;

import java.util.HashSet;

public final class DailyRoutinePersistenceAssembler {

    private DailyRoutinePersistenceAssembler() {}

    public static DailyRoutine toDomain(DailyRoutinePersistenceEntity entity) {
        if (entity == null) return null;

        return new DailyRoutine(
                entity.getId(),
                entity.getPatientId(),
                RoutineTitle.of(entity.getTitle()),
                new HashSet<>(entity.getFrequency()),
                entity.getNotificationStatus(),
                entity.isActive()
        );
    }

    public static void copyToEntity(DailyRoutine domain, DailyRoutinePersistenceEntity entity) {
        entity.setPatientId(domain.getPatientId());
        entity.setTitle(domain.getTitle().value()); 
        entity.setFrequency(new HashSet<>(domain.getFrequency()));
        entity.setNotificationStatus(domain.getNotificationStatus());
        entity.setActive(domain.isActive());
    }
}