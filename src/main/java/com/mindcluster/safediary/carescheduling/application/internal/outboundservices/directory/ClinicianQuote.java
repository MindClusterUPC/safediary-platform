package com.mindcluster.safediary.carescheduling.application.internal.outboundservices.directory;

import java.math.BigDecimal;

/** Verified and published clinician with the rate in force, translated from Clinician Directory. */
public record ClinicianQuote(Long clinicianId, String displayName, BigDecimal amount, String currency, int durationMinutes) {}
