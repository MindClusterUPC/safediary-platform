package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineDetails;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineTitle;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.DailyRoutinePersistenceEntity;

import java.util.HashSet;

public final class DailyRoutinePersistenceAssembler {

    private DailyRoutinePersistenceAssembler() {}

    public static DailyRoutine toDomain(DailyRoutinePersistenceEntity entity) {
        if (entity == null) return null;

        var routine = new DailyRoutine(
                entity.getId(),
                entity.getPatientId(),
                RoutineTitle.of(entity.getTitle()),
                new HashSet<>(entity.getFrequency()),
                entity.getNotificationStatus(),
                entity.isActive()
        );
        routine.updateDetails(new RoutineDetails(
                entity.getDescription(), entity.getScheduledTime(), entity.getDurationMinutes(), entity.getExerciseKind()));
        routine.markCompleted(entity.getCompletedOn());
        return routine;
    }

    public static void copyToEntity(DailyRoutine domain, DailyRoutinePersistenceEntity entity) {
        entity.setPatientId(domain.getPatientId());
        entity.setTitle(domain.getTitle().value()); 
        entity.setFrequency(new HashSet<>(domain.getFrequency()));
        entity.setNotificationStatus(domain.getNotificationStatus());
        entity.setActive(domain.isActive());
        entity.setDescription(domain.getDetails().description());
        entity.setScheduledTime(domain.getDetails().time());
        entity.setDurationMinutes(domain.getDetails().minutes());
        entity.setExerciseKind(domain.getDetails().kind());
        entity.setCompletedOn(domain.getCompletedOn());
    }
}
