package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** joinUrl is only returned when a participant enters the meeting inside its access window. */
public record ClinicalSessionResource(Long id, Long appointmentId, ClinicalSessionStatus status, Instant startedAt,
                                      Instant endedAt, int attendedMinutes, String joinUrl) {}
