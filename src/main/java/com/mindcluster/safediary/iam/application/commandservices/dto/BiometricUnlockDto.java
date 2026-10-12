package com.mindcluster.safediary.iam.application.commandservices.dto;

import java.time.Instant;

/** The raw unlock token is returned only once; the device keeps it behind FaceID or the fingerprint. */
public record BiometricUnlockDto(String unlockToken, Instant expiresAt) {}
