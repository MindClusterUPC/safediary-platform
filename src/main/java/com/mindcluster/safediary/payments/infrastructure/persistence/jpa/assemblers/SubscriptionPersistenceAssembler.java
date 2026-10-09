package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;

public final class SubscriptionPersistenceAssembler {

    private SubscriptionPersistenceAssembler() {}

    public static Subscription toDomain(SubscriptionPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Subscription(
                entity.getId(),
                entity.getPatientAccountId(),
                entity.getPlan(),
                entity.getStatus(),
                entity.getActiveUntil(),
                entity.getStripeSubscriptionId(),
                entity.getStripeCustomerId()
        );
    }

    public static void copyToEntity(Subscription domain, SubscriptionPersistenceEntity entity) {
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setPatientAccountId(domain.getPatientAccountId());
        entity.setPlan(domain.getPlan());
        entity.setStatus(domain.getStatus());
        entity.setActiveUntil(domain.getActiveUntil());
        entity.setStripeSubscriptionId(domain.getStripeSubscriptionId());
        entity.setStripeCustomerId(domain.getStripeCustomerId());
    }
}
