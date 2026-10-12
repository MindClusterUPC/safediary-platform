package com.mindcluster.safediary.iam.domain.model.commands;


public record UnlockVaultCommand(Long accountId, String pin) {}
