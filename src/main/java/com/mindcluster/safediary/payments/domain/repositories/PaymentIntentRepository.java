package com.mindcluster.safediary.payments.domain.repositories;

import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;

import java.util.List;
import java.util.Optional;

public interface PaymentIntentRepository {
    Optional<PaymentIntent> findById(Long id);
    Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentIntent> findByProviderReference(String providerReference);
    List<PaymentIntent> findByPayerAccountId(Long payerAccountId);
    PaymentIntent save(PaymentIntent paymentIntent);
}
