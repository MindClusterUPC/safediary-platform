package com.mindcluster.safediary.iam.domain.model.commands;


public record CreatePrivateVaultCommand(Long accountId, String pin) {}
