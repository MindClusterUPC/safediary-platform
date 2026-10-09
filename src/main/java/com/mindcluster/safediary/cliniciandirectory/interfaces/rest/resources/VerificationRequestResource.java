package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record VerificationRequestResource(Long id, Long clinicianId, String licenseNumber, String specialty, String documentRef, VerificationStatus status,
        Instant submittedAt, Long reviewedByAccountId, Instant reviewedAt, String rejectionReason) {}
