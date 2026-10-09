package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record ContactRequestResource(Long id, Long patientAccountId, Long clinicianId, ContactRequestStatus status,
                                     Instant createdAt, CoordinationMessageResource lastMessage) {}
