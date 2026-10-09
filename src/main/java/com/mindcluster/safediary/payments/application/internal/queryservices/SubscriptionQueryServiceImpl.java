package com.mindcluster.safediary.payments.application.internal.queryservices;

import com.mindcluster.safediary.payments.application.queryservices.SubscriptionQueryService;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.queries.GetSubscriptionByPatientIdQuery;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionQueryServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public Result<Subscription, ApplicationError> handle(GetSubscriptionByPatientIdQuery query) {
        try {
            Long patientAccountId = query.patientAccountId();
            Subscription subscription = subscriptionRepository.findByPatientAccountId(patientAccountId)
                    .orElseGet(() -> {
                        Subscription freeSub = Subscription.createFree(patientAccountId);
                        return subscriptionRepository.save(freeSub);
                    });
            return Result.success(subscription);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("subscription-query", ex.getMessage()));
        }
    }
}
