package com.mindcluster.safediary.profiles.domain.model.commands;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePsychologistProfileCommand(
        Long psychologistId,
        BigDecimal costPerSession,
        int durationMinutes,
        String currency,
        boolean videoCallEnabled,
        boolean encryptedChatEnabled,
        String bio,
        List<String> categories,
        List<String> certificateTitles
) {}
