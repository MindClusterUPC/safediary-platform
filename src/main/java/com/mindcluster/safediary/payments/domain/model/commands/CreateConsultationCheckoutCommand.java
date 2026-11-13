package com.mindcluster.safediary.payments.domain.model.commands;

/**
 * Starts the checkout of a held appointment. The patient comes from the bearer token; the amount, currency and
 * psychologist are resolved on the server from Care Scheduling and Clinician Directory.
 */
public record CreateConsultationCheckoutCommand(
        Long patientAccountId,
        Long appointmentId,
        String successUrl,
        String cancelUrl
) {
    public CreateConsultationCheckoutCommand {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be positive");
        }
        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("appointmentId must be positive");
        }
    }
}
