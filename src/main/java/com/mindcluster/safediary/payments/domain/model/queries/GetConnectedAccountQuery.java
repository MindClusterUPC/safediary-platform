package com.mindcluster.safediary.payments.domain.model.queries;

public record GetConnectedAccountQuery(Long psychologistAccountId) {
    public GetConnectedAccountQuery {
        if (psychologistAccountId == null || psychologistAccountId <= 0) {
            throw new IllegalArgumentException("psychologistAccountId must be positive");
        }
    }
}
