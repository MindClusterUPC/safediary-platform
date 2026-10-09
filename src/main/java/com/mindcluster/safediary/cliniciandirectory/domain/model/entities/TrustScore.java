package com.mindcluster.safediary.cliniciandirectory.domain.model.entities;

import java.math.BigDecimal;
/** Quality factor is independent of attention volume; session totals are explanatory context only. */
public record TrustScore(Long clinicianId, BigDecimal score, boolean sufficientData, BigDecimal ratingFactor,
                         long completedSessions, long completedMinutes, int reviewCount) {}
