package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record PublishReviewResource(@NotNull @Positive Long appointmentId, @Min(1) @Max(5) int rating, @Size(max=2000) String text) {}
