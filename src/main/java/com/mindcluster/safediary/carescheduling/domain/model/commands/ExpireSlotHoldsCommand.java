package com.mindcluster.safediary.carescheduling.domain.model.commands;

import java.time.Instant;

public record ExpireSlotHoldsCommand(Instant now) {}
