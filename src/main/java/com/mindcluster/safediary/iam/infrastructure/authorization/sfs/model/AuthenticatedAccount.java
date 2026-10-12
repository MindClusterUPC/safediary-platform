package com.mindcluster.safediary.iam.infrastructure.authorization.sfs.model;

import com.mindcluster.safediary.iam.domain.model.valueobjects.AccountRole;

/** Principal stored in the security context once the bearer token is validated. */
public record AuthenticatedAccount(Long accountId, String email, AccountRole role) {}
