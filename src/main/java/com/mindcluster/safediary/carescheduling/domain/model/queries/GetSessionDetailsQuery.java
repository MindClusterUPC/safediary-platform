package com.mindcluster.safediary.carescheduling.domain.model.queries;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

public record GetSessionDetailsQuery(CareActor actor, Long appointmentId) {}
