package com.mindcluster.safediary.carescheduling.application.internal.outboundservices.summaries;

import java.util.Optional;

public interface EmotionalSummaryClient { Optional<EmotionalSummaryDto> fetchLatestSummary(Long patientAccountId); }
