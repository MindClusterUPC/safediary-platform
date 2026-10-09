package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record ReviewReportResource(Long id, Long reviewId, ReportReason reason, String comment, ReportStatus status,
        Instant reportedAt, Long resolvedByAccountId, Instant resolvedAt, String resolution) {}
