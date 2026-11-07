package com.mindcluster.safediary.iam.application.commandservices.dto;

import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import java.time.Instant;

/** Session started by sign-up, sign-in, federated or biometric sign-in. */
public record AuthenticatedAccountDto(Account account, String accessToken, Instant expiresAt) {}
