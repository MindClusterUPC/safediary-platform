package com.mindcluster.safediary.iam.interfaces.rest.resources;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** role is PATIENT (default) or PSYCHOLOGIST; the psychologist then completes the Clinician Directory verification. */
public record RegisterAccountResource(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=72) String password, AccountRole role) {}
