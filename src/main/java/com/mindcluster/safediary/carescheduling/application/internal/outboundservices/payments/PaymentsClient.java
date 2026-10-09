package com.mindcluster.safediary.carescheduling.application.internal.outboundservices.payments;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.Appointment;

/** Care Scheduling asks Payments and Payouts for charges and refunds; it never creates payment intents itself. */
public interface PaymentsClient {
    /** Requests the charge of a held reservation and returns its idempotent payment reference. */
    String requestAppointmentCharge(Appointment appointment);

    void requestRefund(Appointment appointment, String reason);
}
