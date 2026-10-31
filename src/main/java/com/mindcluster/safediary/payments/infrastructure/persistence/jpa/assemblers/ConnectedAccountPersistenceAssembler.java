package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.payments.domain.model.aggregates.ConnectedAccount;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.ConnectedAccountPersistenceEntity;

public final class ConnectedAccountPersistenceAssembler {

    private ConnectedAccountPersistenceAssembler() {}

    public static ConnectedAccount toDomain(ConnectedAccountPersistenceEntity entity) {
        if (entity == null) return null;
        return new ConnectedAccount(
                entity.getId(),
                entity.getPsychologistAccountId(),
                entity.getStripeAccountId(),
                entity.isChargesEnabled(),
                entity.isPayoutsEnabled(),
                entity.isDetailsSubmitted(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant() : null,
                entity.getUpdatedAt() != null ? entity.getUpdatedAt().toInstant() : null
        );
    }

    public static ConnectedAccountPersistenceEntity toEntity(ConnectedAccount domain) {
        if (domain == null) return null;
        ConnectedAccountPersistenceEntity entity = new ConnectedAccountPersistenceEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setPsychologistAccountId(domain.getPsychologistAccountId());
        entity.setStripeAccountId(domain.getStripeAccountId());
        entity.setChargesEnabled(domain.isChargesEnabled());
        entity.setPayoutsEnabled(domain.isPayoutsEnabled());
        entity.setDetailsSubmitted(domain.isDetailsSubmitted());
        return entity;
    }
}
