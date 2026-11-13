package com.mindcluster.safediary.payments.domain.model.commands;

public record SkipConsultationPaymentCommand(
        Long patientAccountId,
        Long appointmentId
) {
    public SkipConsultationPaymentCommand {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be positive");
        }
        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("appointmentId must be positive");
        }
    }
}
