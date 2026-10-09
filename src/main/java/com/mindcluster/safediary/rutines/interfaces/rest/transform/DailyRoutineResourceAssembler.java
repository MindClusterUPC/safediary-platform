package com.mindcluster.safediary.rutines.interfaces.rest.transform;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.DailyRoutineResponseResource;

import java.util.stream.Collectors;

public final class DailyRoutineResourceAssembler {

    private DailyRoutineResourceAssembler() {}

    public static DailyRoutineResponseResource toResource(DailyRoutine routine) {
        if (routine == null) return null;

        var frequencyDays = routine.getFrequency().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new DailyRoutineResponseResource(
                routine.getId(),
                routine.getPatientId(),
                routine.getTitle().value(),
                frequencyDays,
                routine.getNotificationStatus().name(),
                routine.isActive()
        );
    }
}