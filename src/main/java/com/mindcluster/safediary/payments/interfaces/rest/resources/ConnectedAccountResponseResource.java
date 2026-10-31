package com.mindcluster.safediary.payments.interfaces.rest.resources;

public record ConnectedAccountResponseResource(
        Long psychologistAccountId,
        String stripeAccountId,
        String onboardingUrl,
        boolean chargesEnabled,
        boolean payoutsEnabled,
        boolean detailsSubmitted,
        boolean ready
) {}
