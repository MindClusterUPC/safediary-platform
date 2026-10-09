package com.mindcluster.safediary.carescheduling.domain.model.valueobjects;

import java.time.Instant;

public record BookableSlot(Instant startsAt, Instant endsAt, SlotAvailability availability) {}
