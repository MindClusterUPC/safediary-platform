package com.mindcluster.safediary.payments.application.commandservices.dto;

import java.math.BigDecimal;

public record SkipConsultationPaymentResultDto(
        Long appointmentId,
        Long patientAccountId,
        String appointmentStatus,
        BigDecimal amount,
        String currency,
        String reference
) {
}
