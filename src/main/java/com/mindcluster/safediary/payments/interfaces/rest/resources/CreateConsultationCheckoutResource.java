package com.mindcluster.safediary.payments.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * The appointment to pay. Psychologist and amount are never taken from the client; patientAccountId is optional
 * and, when sent, must be the signed-in patient.
 */
public record CreateConsultationCheckoutResource(
        Long patientAccountId,
        @NotNull @Positive Long appointmentId,
        String successUrl,
        String cancelUrl
) {}
