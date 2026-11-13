package com.mindcluster.safediary.payments.application.commandservices.dto;

public record PaymentMethodSetupResultDto(
        Long patientAccountId,
        String checkoutUrl,
        String sessionId,
        String message
) {}
