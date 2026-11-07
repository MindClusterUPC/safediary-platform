package com.mindcluster.safediary.payments.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateConsultationCheckoutResource(
        @NotNull @Positive Long patientAccountId,
        @NotNull @Positive Long psychologistAccountId,
        @NotNull @Positive Long appointmentId,
        @NotNull @DecimalMin("1.00") BigDecimal totalAmount,
        BigDecimal platformFee,
        String currency,
        String successUrl,
        String cancelUrl
) {}
