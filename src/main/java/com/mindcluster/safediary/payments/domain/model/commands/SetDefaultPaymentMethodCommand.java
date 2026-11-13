package com.mindcluster.safediary.payments.domain.model.commands;

/**
 * Command to set a saved Stripe payment method as the patient's default invoice/checkout method.
 */
public record SetDefaultPaymentMethodCommand(
        Long patientAccountId,
        String paymentMethodId
) {
    public SetDefaultPaymentMethodCommand {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be a positive non-null ID");
        }
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            throw new IllegalArgumentException("paymentMethodId is required");
        }
    }
}
