package com.mindcluster.safediary.payments.infrastructure.events;

import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentChargeRequestedDto;
import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;
import com.mindcluster.safediary.payments.domain.repositories.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentChargeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentChargeEventConsumer.class);

    private final PaymentIntentRepository paymentIntentRepository;

    @EventListener
    public void onAppointmentChargeRequested(AppointmentChargeRequestedDto event) {
        log.info("Received AppointmentChargeRequestedDto for appointment {}, hold expires at {}",
                event.appointmentId(), event.holdExpiresAt());

        paymentIntentRepository.findByIdempotencyKey(event.idempotencyKey()).orElseGet(() -> {
            PaymentIntent pi = new PaymentIntent(
                    "CONSULTATION",
                    event.appointmentId(),
                    event.patientAccountId(),
                    event.amount(),
                    event.currency(),
                    event.idempotencyKey()
            );
            return paymentIntentRepository.save(pi);
        });
    }
}
