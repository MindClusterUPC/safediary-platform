package com.mindcluster.safediary.payments.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateConnectedAccountResource(
        @NotNull @Positive Long psychologistAccountId,
        String email,
        String refreshUrl,
        String returnUrl
) {}
