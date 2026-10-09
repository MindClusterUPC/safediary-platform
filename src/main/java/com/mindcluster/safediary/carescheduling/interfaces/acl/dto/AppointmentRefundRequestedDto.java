package com.mindcluster.safediary.carescheduling.interfaces.acl.dto;

/**
 * Published Language event for Payments and Payouts: a charge must be reconciled or refunded according to its policy.
 * Reasons: LATE_PAYMENT, DUPLICATE_PAYMENT, CANCELLED_BY_PATIENT, CANCELLED_BY_CLINICIAN, PATIENT_NO_SHOW, SESSION_NOT_HELD.
 */
public record AppointmentRefundRequestedDto(Long appointmentId, String paymentReference, String reason) {}
