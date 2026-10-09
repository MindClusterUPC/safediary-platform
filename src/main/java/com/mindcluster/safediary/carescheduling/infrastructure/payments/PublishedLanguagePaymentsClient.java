package com.mindcluster.safediary.carescheduling.infrastructure.payments;

import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.payments.PaymentsClient;
import com.mindcluster.safediary.carescheduling.domain.model.aggregates.Appointment;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Customer/Supplier integration with Payments and Payouts through Published Language events. Payments consumes
 * AppointmentChargeRequestedDto and AppointmentRefundRequestedDto, and answers with AppointmentPaymentResultDto.
 * The payment reference is stable per reservation, so retries never create a second charge.
 */
@Component @RequiredArgsConstructor
public class PublishedLanguagePaymentsClient implements PaymentsClient {
    private final ApplicationEventPublisher events;

    public String requestAppointmentCharge(Appointment appointment) {
        var reference = "care-appointment-" + appointment.getId();
        events.publishEvent(new AppointmentChargeRequestedDto(appointment.getId(), appointment.getPatientAccountId(),
                appointment.getClinicianId(), appointment.getAmount().amount(), appointment.getAmount().currency(),
                reference, appointment.getHold().getExpiresAt()));
        return reference;
    }

    public void requestRefund(Appointment appointment, String reason) {
        if (appointment.getPaymentReference() == null) return;
        events.publishEvent(new AppointmentRefundRequestedDto(appointment.getId(), appointment.getPaymentReference(), reason));
    }
}
