package com.mindcluster.safediary.iam.domain.model.commands;


public record ProtectEntryInVaultCommand(Long accountId, String pin, Long diaryEntryId) {}
