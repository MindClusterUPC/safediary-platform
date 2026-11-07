package com.mindcluster.safediary.payments.infrastructure.events;

import com.mindcluster.safediary.iam.domain.model.events.IamDomainEvent;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event consumer listening to IAM events to automatically provision
 * default subscription states (e.g., FREE plan) when an account is registered.
 */
@Component
@RequiredArgsConstructor
public class IamAccountEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(IamAccountEventConsumer.class);

    private final SubscriptionRepository subscriptionRepository;
    private final IamContextFacade iamContextFacade;

    @EventListener
    public void onAccountRegistered(IamDomainEvent event) {
        if (!"AccountRegistered".equalsIgnoreCase(event.type())) {
            return;
        }

        Long accountId = event.accountId();
        if (accountId == null || accountId <= 0) {
            return;
        }

        var accountDtoOpt = iamContextFacade.fetchAccount(accountId);
        if (accountDtoOpt.isEmpty()) {
            log.warn("AccountRegistered event received for accountId={}, but account not found in IAM", accountId);
            return;
        }

        var accountDto = accountDtoOpt.get();
        if ("PATIENT".equalsIgnoreCase(accountDto.role())) {
            if (!subscriptionRepository.existsByPatientAccountId(accountId)) {
                Subscription freeSub = Subscription.createFree(accountId);
                subscriptionRepository.save(freeSub);
                log.info("Initialized default FREE subscription for newly registered patient accountId={}", accountId);
            }
        }
    }
}
