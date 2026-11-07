package com.mindcluster.safediary.payments.application.outbound.dto;

public record StripePaymentMethodDto(
        String id,
        String brand,
        String last4,
        String expiry,
        String cardholder,
        boolean isDefault,
        String note
) {}
