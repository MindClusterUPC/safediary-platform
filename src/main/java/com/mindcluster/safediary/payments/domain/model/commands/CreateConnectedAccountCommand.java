package com.mindcluster.safediary.payments.domain.model.commands;

public record CreateConnectedAccountCommand(
        Long psychologistAccountId,
        String email,
        String refreshUrl,
        String returnUrl
) {
    public CreateConnectedAccountCommand {
        if (psychologistAccountId == null || psychologistAccountId <= 0) {
            throw new IllegalArgumentException("psychologistAccountId must be positive");
        }
    }
}
