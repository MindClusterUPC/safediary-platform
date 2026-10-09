package com.mindcluster.safediary.carescheduling.application.commandservices.dto;

import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.summaries.EmotionalSummaryDto;
import java.time.Instant;

public record AuthorizedSummaryDto(Long appointmentId, Long patientAccountId, String consentRef, Instant accessedAt,
                                   EmotionalSummaryDto summary) {}
