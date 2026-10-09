package com.mindcluster.safediary.carescheduling.domain.model.commands;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;

/** accept=true opens the chat; accept=false rejects the contact request (US-043). */
public record RespondToContactRequestCommand(CareActor actor, Long contactRequestId, boolean accept) {}
