package com.mindcluster.safediary.payments.interfaces.rest.resources;

import java.math.BigDecimal;

public record SkipConsultationPaymentResponseResource(
        Long appointmentId,
        Long patientAccountId,
        String appointmentStatus,
        BigDecimal amount,
        String currency,
        String reference
) {
}
