package com.mindcluster.safediary.carescheduling.domain.model.commands;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

import java.time.Instant;

public record ProposeScheduleCommand(CareActor actor, Long contactRequestId, Instant startsAt) {}
