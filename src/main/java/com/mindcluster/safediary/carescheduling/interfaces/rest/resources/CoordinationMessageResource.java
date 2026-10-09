package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record CoordinationMessageResource(Long id, Long contactRequestId, Long senderAccountId, String body, Instant sentAt) {}
