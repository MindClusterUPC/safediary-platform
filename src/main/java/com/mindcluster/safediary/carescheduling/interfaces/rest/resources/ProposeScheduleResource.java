package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/** startsAt is an ISO-8601 instant, for example 2026-10-09T20:00:00Z (15:00 in America/Lima). */
public record ProposeScheduleResource(@NotNull @Positive Long contactRequestId, @NotNull @Future Instant startsAt) {}
