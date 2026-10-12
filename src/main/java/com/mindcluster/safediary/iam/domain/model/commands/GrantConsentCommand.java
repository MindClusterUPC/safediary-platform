package com.mindcluster.safediary.iam.domain.model.commands;

import com.mindcluster.safediary.iam.domain.model.entities.SharingPermission;
import java.time.Instant;
import java.util.List;

public record GrantConsentCommand(Long ownerAccountId, Long granteeAccountId, List<SharingPermission> permissions, Instant expiresAt) {}
