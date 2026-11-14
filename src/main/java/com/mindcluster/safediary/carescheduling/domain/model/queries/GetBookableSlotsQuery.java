package com.mindcluster.safediary.carescheduling.domain.model.queries;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

import java.time.LocalDate;

public record GetBookableSlotsQuery(CareActor actor, Long clinicianId, LocalDate date, Integer stepMinutes) {
    public GetBookableSlotsQuery(CareActor actor, Long clinicianId, LocalDate date) {
        this(actor, clinicianId, date, null);
    }
}
