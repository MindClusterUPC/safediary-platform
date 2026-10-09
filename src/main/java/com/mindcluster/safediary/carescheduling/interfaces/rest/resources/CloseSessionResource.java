package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/** COMPLETED, NO_SHOW or NOT_HELD (cancellation or technical problem). */
public record CloseSessionResource(@NotNull SessionOutcome outcome) {}
