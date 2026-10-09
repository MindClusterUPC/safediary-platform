package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record AuthorizedSummaryResource(Long appointmentId, Long patientAccountId, String consentRef, Instant accessedAt,
                                        LocalDate periodStart, LocalDate periodEnd, List<String> dominantEmotions,
                                        List<String> keyTriggers, String synthesisNarrative, List<String> highlights) {}
