package com.mindcluster.safediary.iam.interfaces.rest.resources;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** idToken returned by Google Sign-In or Sign in with Apple on the device; role is used only for new accounts. */
public record FederatedAuthenticationResource(@NotNull AuthProviderType provider, @NotBlank String idToken, AccountRole role) {}
