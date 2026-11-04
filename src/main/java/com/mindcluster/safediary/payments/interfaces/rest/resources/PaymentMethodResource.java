package com.mindcluster.safediary.payments.interfaces.rest.resources;

public record PaymentMethodResource(
        String id,
        String brand,
        String last4,
        String expiry,
        String cardholder,
        boolean isDefault,
        String note
) {}
