package com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto;

import java.time.Instant;
/** Trusted Care Scheduling integration contract, never accepted from a public REST endpoint. */
public record CompletedCareSessionDto(Long appointmentId, Long clinicianId, Long patientAccountId,
                                      int durationMinutes, Instant completedAt) {}
