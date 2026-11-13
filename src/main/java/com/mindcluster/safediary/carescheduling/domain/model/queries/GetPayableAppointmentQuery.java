package com.mindcluster.safediary.carescheduling.domain.model.queries;

/** Internal query for Payments and Payouts: the patient's appointment whose slot hold is still waiting for payment. */
public record GetPayableAppointmentQuery(Long appointmentId, Long patientAccountId) {}
