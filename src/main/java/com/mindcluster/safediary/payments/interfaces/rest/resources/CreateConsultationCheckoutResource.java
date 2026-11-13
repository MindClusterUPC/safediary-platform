package com.mindcluster.safediary.payments.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Only the appointment is sent: patient, psychologist and amount are never taken from the client. */
public record CreateConsultationCheckoutResource(
        @NotNull @Positive Long appointmentId,
        String successUrl,
        String cancelUrl
) {}
