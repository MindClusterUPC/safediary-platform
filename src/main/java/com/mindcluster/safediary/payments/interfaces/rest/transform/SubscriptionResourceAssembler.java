package com.mindcluster.safediary.payments.interfaces.rest.transform;

import com.mindcluster.safediary.payments.application.commandservices.dto.SubscriptionCheckoutResultDto;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.interfaces.rest.resources.CheckoutResponseResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.SubscriptionResponseResource;

public final class SubscriptionResourceAssembler {

    private SubscriptionResourceAssembler() {}

    public static SubscriptionResponseResource toResource(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        return new SubscriptionResponseResource(
                subscription.getId(),
                subscription.getPatientAccountId(),
                subscription.getPlan().name(),
                subscription.getStatus().name(),
                subscription.getActiveUntil(),
                subscription.isActive(),
                subscription.getStripeSubscriptionId()
        );
    }

    public static CheckoutResponseResource toResource(SubscriptionCheckoutResultDto dto) {
        if (dto == null) {
            return null;
        }
        return new CheckoutResponseResource(
                dto.patientAccountId(),
                dto.plan().name(),
                dto.checkoutUrl(),
                dto.sessionId(),
                dto.checkoutRequired(),
                dto.message()
        );
    }
}
