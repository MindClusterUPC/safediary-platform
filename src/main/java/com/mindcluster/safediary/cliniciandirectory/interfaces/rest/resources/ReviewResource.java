package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record ReviewResource(Long id, Long clinicianId, Integer rating, String text, Instant publishedAt, long helpfulCount) {}
