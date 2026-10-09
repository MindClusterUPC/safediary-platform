package com.mindcluster.safediary.carescheduling.domain.model.commands;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

public record JoinSessionCommand(CareActor actor, Long appointmentId) {}
