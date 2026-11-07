package com.mindcluster.safediary.payments.interfaces.rest.resources;

import java.math.BigDecimal;

public record ConsultationCheckoutResponseResource(
        Long appointmentId,
        Long patientAccountId,
        Long psychologistAccountId,
        BigDecimal amount,
        String currency,
        String checkoutUrl,
        String sessionId
) {}
