package com.mindcluster.safediary.payments.domain.model.commands;

/**
 * Command to initialize a Stripe Checkout Session in setup mode so a patient can save a card or wallet.
 */
public record CreatePaymentMethodSetupCommand(
        Long patientAccountId,
        String successUrl,
        String cancelUrl
) {
    public CreatePaymentMethodSetupCommand {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be a positive non-null ID");
        }
    }
}
