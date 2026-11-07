package com.mindcluster.safediary.payments.domain.model.commands;

import java.math.BigDecimal;

public record CreateConsultationCheckoutCommand(
        Long patientAccountId,
        Long psychologistAccountId,
        Long appointmentId,
        BigDecimal totalAmount,
        BigDecimal platformFee,
        String currency,
        String successUrl,
        String cancelUrl,
        String idempotencyKey
) {
    public CreateConsultationCheckoutCommand {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be positive");
        }
        if (psychologistAccountId == null || psychologistAccountId <= 0) {
            throw new IllegalArgumentException("psychologistAccountId must be positive");
        }
        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("appointmentId must be positive");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than zero");
        }
    }
}
