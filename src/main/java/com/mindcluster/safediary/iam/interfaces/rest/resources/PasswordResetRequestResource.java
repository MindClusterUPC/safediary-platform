package com.mindcluster.safediary.iam.interfaces.rest.resources;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record PasswordResetRequestResource(@NotBlank @Email String email) {}
