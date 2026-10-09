package com.mindcluster.safediary.payments.interfaces.acl.internal;

import com.mindcluster.safediary.payments.application.queryservices.SubscriptionQueryService;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.queries.GetSubscriptionByPatientIdQuery;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionStatus;
import com.mindcluster.safediary.payments.interfaces.acl.PaymentsContextFacade;
import com.mindcluster.safediary.payments.interfaces.acl.dto.PatientPlanStatusDto;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.stereotype.Component;

@Component
public class PaymentsContextFacadeImpl implements PaymentsContextFacade {

    private final SubscriptionQueryService subscriptionQueryService;

    public PaymentsContextFacadeImpl(SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @Override
    public PatientPlanStatusDto fetchPatientPlanStatus(Long patientAccountId) {
        if (patientAccountId == null || patientAccountId <= 0) {
            return new PatientPlanStatusDto(
                    patientAccountId,
                    SubscriptionPlan.FREE.name(),
                    SubscriptionStatus.ACTIVE.name(),
                    null,
                    true
            );
        }

        var result = subscriptionQueryService.handle(new GetSubscriptionByPatientIdQuery(patientAccountId));
        if (result instanceof Result.Success<Subscription, ?> success) {
            Subscription sub = success.value();
            return new PatientPlanStatusDto(
                    sub.getPatientAccountId(),
                    sub.getPlan().name(),
                    sub.getStatus().name(),
                    sub.getActiveUntil(),
                    sub.isActive()
            );
        }

        return new PatientPlanStatusDto(
                patientAccountId,
                SubscriptionPlan.FREE.name(),
                SubscriptionStatus.ACTIVE.name(),
                null,
                true
        );
    }
}
