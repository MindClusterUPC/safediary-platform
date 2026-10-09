package com.mindcluster.safediary.carescheduling.application.internal.outboundservices.summaries;

import java.time.LocalDate;
import java.util.List;

/** Minimal authorized emotional summary; the full conversation or diary never reaches Care Scheduling. */
public record EmotionalSummaryDto(LocalDate periodStart, LocalDate periodEnd, List<String> dominantEmotions,
                                  List<String> keyTriggers, String synthesisNarrative, List<String> highlights) {}
