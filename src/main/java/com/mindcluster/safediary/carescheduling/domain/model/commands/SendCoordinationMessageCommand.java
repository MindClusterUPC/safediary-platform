package com.mindcluster.safediary.carescheduling.domain.model.commands;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

public record SendCoordinationMessageCommand(CareActor actor, Long contactRequestId, String body) {}
