package com.mindcluster.safediary.carescheduling.interfaces.acl.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Appointment held for the patient and waiting for payment. Payments and Payouts charges exactly this amount and
 * answers with AppointmentPaymentResultDto using the same payment reference.
 */
public record PayableAppointmentDto(Long appointmentId, Long patientAccountId, Long clinicianId, BigDecimal amount,
                                    String currency, String paymentReference, Instant holdExpiresAt) {}
