package com.mindcluster.safediary.rutines.domain.model.events;

public record DailyReflectionSubmittedEvent(Long reflectionId, Long patientId, String promptText, String answer) {}