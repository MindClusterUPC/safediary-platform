package com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto;

import java.math.BigDecimal;
public record PublishedClinicianRateDto(Long clinicianId, String displayName, BigDecimal amount,
                                       String currency, int durationMinutes, int version) {}
