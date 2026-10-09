package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record TrustScoreResource(Long clinicianId, BigDecimal score, boolean sufficientData, BigDecimal ratingFactor,
        long completedSessions, long completedMinutes, int reviewCount, int minimumReviews, String methodology) {}
