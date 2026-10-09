package com.mindcluster.safediary.payments.interfaces.acl.dto;

import java.time.Instant;

/**
 * Cross-context DTO exposing patient plan subscription status.
 */
public record PatientPlanStatusDto(
        Long patientAccountId,
        String plan,
        String status,
        Instant activeUntil,
        boolean active
) {}
