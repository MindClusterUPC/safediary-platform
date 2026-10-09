package com.mindcluster.safediary.carescheduling.domain.model.commands;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

public record CompleteSessionCommand(CareActor actor, Long appointmentId, SessionOutcome outcome) {}
