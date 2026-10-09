package com.mindcluster.safediary.profiles.interfaces.acl.dto;

import java.math.BigDecimal;

/**
 * Public DTO exposed to external bounded contexts (like Payments and Care Scheduling)
 * providing verified session pricing and duration without leaking the aggregate root.
 */
public record ClinicianConsultationRateDto(
        Long psychologistId,
        String fullName,
        BigDecimal costPerSession,
        int durationMinutes,
        String currency,
        boolean acceptsVideoCall,
        boolean acceptsEncryptedChat
) {}
