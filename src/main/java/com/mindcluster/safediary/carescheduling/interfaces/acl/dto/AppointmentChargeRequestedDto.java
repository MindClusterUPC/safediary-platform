package com.mindcluster.safediary.carescheduling.interfaces.acl.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published Language event: Care Scheduling asks Payments and Payouts to charge a held reservation. The idempotency
 * key is stable for the hold, so retries never create a second charge.
 */
public record AppointmentChargeRequestedDto(Long appointmentId, Long patientAccountId, Long clinicianId, BigDecimal amount,
                                            String currency, String idempotencyKey, Instant holdExpiresAt) {}
