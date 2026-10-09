package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

/** true opens the chat ("Abrir chat"); false rejects the request ("Rechazar"). */
public record RespondContactRequestResource(@NotNull Boolean accept) {}
