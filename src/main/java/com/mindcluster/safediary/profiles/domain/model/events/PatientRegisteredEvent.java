package com.mindcluster.safediary.profiles.domain.model.events;

public record PatientRegisteredEvent(Long patientId, String email) {}
