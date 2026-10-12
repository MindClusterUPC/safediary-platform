package com.mindcluster.safediary.iam.interfaces.rest.resources;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Send accessToken as "Authorization: Bearer <token>" to every protected endpoint. */
public record AuthenticatedAccountResource(Long accountId, String email, AccountRole role, String accessToken, String tokenType,
                                           Instant expiresAt) {}
