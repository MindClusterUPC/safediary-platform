package com.mindcluster.safediary.carescheduling.interfaces.acl.dto;

import java.math.BigDecimal;

/**
 * Trusted contract published by Payments and Payouts after verifying the gateway signature (PaymentApproved or
 * PaymentFailed for an appointment). Never accepted from a public REST endpoint.
 */
public record AppointmentPaymentResultDto(Long appointmentId, String paymentReference, BigDecimal amount, String currency,
                                          boolean approved) {}
