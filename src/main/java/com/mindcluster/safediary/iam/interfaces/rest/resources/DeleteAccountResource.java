package com.mindcluster.safediary.iam.interfaces.rest.resources;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** The password confirms the identity again; accounts created only with Google or Apple send null. */
public record DeleteAccountResource(String password) {}
