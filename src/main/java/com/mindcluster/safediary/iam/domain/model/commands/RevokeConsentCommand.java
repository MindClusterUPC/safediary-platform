package com.mindcluster.safediary.iam.domain.model.commands;


public record RevokeConsentCommand(Long ownerAccountId, Long consentId) {}
