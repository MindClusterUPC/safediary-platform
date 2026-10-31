package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity;

public final class PaymentIntentPersistenceAssembler {

    private PaymentIntentPersistenceAssembler() {}

    public static PaymentIntent toDomain(PaymentIntentPersistenceEntity entity) {
        if (entity == null) return null;
        return new PaymentIntent(
                entity.getId(),
                entity.getPurpose(),
                entity.getSubjectId(),
                entity.getPayerAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getIdempotencyKey(),
                entity.getProviderReference(),
                entity.getStatus()
        );
    }

    public static PaymentIntentPersistenceEntity toEntity(PaymentIntent domain) {
        if (domain == null) return null;
        PaymentIntentPersistenceEntity entity = new PaymentIntentPersistenceEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setPurpose(domain.getPurpose());
        entity.setSubjectId(domain.getSubjectId());
        entity.setPayerAccountId(domain.getPayerAccountId());
        entity.setAmount(domain.getAmount());
        entity.setCurrency(domain.getCurrency());
        entity.setIdempotencyKey(domain.getIdempotencyKey());
        entity.setProviderReference(domain.getProviderReference());
        entity.setStatus(domain.getStatus());
        return entity;
    }
}
