package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record PsychologistResponseResource(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        SessionPricingResource pricing,
        ModalityOptionsResource modalities,
        String bio,
        List<String> categories,
        List<String> certificateTitles,
        List<AvailabilitySlotResponseResource> availability
) {
    public record SessionPricingResource(
            BigDecimal costPerSession,
            int durationMinutes,
            String currency
    ) {}

    public record ModalityOptionsResource(
            boolean videoCallEnabled,
            boolean encryptedChatEnabled
    ) {}

    public record AvailabilitySlotResponseResource(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {}
}
