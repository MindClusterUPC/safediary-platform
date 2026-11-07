package com.mindcluster.safediary.payments.application.queryservices.dto;

public record ConnectedAccountDto(
        Long psychologistAccountId,
        String stripeAccountId,
        boolean chargesEnabled,
        boolean payoutsEnabled,
        boolean detailsSubmitted,
        boolean ready
) {}
