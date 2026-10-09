package com.mindcluster.safediary.cliniciandirectory.domain.model.commands;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.math.BigDecimal;
import java.util.List;
public record RecordCompletedSessionCommand(Long appointmentId, Long clinicianId, Long patientAccountId, int durationMinutes, java.time.Instant completedAt) {}
