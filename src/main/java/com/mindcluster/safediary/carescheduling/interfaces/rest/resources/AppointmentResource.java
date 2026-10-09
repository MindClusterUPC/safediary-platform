package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** Used for proposals (REQUESTED), temporary reservations (HELD) and appointments. */
public record AppointmentResource(Long id, Long contactRequestId, Long patientAccountId, Long clinicianId, Instant startsAt,
                                  Instant endsAt, String timezone, BigDecimal amount, String currency, AppointmentStatus status,
                                  String paymentReference, Instant holdExpiresAt, long holdRemainingSeconds,
                                  Instant accessOpensAt, Instant accessClosesAt) {}
