package com.mindcluster.safediary.carescheduling.domain.model.commands;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

import java.util.List;

public record SetWeeklyAvailabilityCommand(CareActor actor, List<AvailabilityWindow> windows) {}
