package com.mindcluster.safediary.payments.application.commandservices;

import com.mindcluster.safediary.payments.application.commandservices.dto.SubscriptionCheckoutResultDto;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.commands.CancelSubscriptionCommand;
import com.mindcluster.safediary.payments.domain.model.commands.CreateSubscriptionCheckoutCommand;
import com.mindcluster.safediary.payments.domain.model.commands.ProcessStripeWebhookCommand;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface SubscriptionCommandService {
    Result<SubscriptionCheckoutResultDto, ApplicationError> handle(CreateSubscriptionCheckoutCommand command);
    Result<Subscription, ApplicationError> handle(CancelSubscriptionCommand command);
    Result<Void, ApplicationError> handle(ProcessStripeWebhookCommand command);
}
