package com.mindcluster.safediary.cliniciandirectory.domain.model.entities;

import java.math.BigDecimal;
public record RatingSummary(Long clinicianId, BigDecimal average, int reviewCount) {}
