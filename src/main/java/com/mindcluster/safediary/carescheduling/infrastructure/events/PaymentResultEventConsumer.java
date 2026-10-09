package com.mindcluster.safediary.carescheduling.infrastructure.events;

import com.mindcluster.safediary.carescheduling.interfaces.acl.CareSchedulingContextFacade;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentPaymentResultDto;
import com.mindcluster.safediary.shared.application.result.Result;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Consumes PaymentApproved and PaymentFailed results that Payments and Payouts publishes for appointments. */
@Component @RequiredArgsConstructor
public class PaymentResultEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResultEventConsumer.class);
    private final CareSchedulingContextFacade facade;

    @EventListener public void onPaymentResult(AppointmentPaymentResultDto event) {
        var result = facade.applyPaymentResult(event);
        if (result instanceof Result.Failure<?, ?> failure)
            LOGGER.warn("Payment result for appointment {} was not applied: {}", event.appointmentId(), failure.error());
        else
            LOGGER.info("Payment result for appointment {}: {}", event.appointmentId(), result.getOrElse(null));
    }
}
