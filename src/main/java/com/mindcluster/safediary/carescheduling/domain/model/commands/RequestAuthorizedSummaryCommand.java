package com.mindcluster.safediary.carescheduling.domain.model.commands;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

public record RequestAuthorizedSummaryCommand(CareActor actor, Long appointmentId) {}
