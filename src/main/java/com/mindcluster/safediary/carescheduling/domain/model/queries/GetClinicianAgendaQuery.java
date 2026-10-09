package com.mindcluster.safediary.carescheduling.domain.model.queries;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

import java.time.Instant;

public record GetClinicianAgendaQuery(CareActor actor, Instant from, Instant to) {}
