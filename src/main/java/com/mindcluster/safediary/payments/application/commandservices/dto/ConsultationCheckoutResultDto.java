package com.mindcluster.safediary.payments.application.commandservices.dto;

import java.math.BigDecimal;

public record ConsultationCheckoutResultDto(
        Long appointmentId,
        Long patientAccountId,
        Long psychologistAccountId,
        BigDecimal amount,
        String currency,
        String checkoutUrl,
        String sessionId
) {}
