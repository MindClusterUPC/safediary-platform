package com.mindcluster.safediary.payments.application.commandservices.dto;

public record ConnectedAccountOnboardingDto(
        Long psychologistAccountId,
        String stripeAccountId,
        String onboardingUrl,
        boolean chargesEnabled,
        boolean payoutsEnabled,
        boolean detailsSubmitted
) {}
