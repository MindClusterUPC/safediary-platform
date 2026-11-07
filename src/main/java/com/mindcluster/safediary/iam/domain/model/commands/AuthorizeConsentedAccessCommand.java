package com.mindcluster.safediary.iam.domain.model.commands;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;

import java.time.LocalDate;

public record AuthorizeConsentedAccessCommand(Long ownerAccountId, Long granteeAccountId, SharingScope scope, LocalDate date, String sourceContext,
                                              String resourceType, Long resourceId) {}
