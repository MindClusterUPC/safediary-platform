package com.mindcluster.safediary.iam.domain.model.commands;


public record RequestAccountDeletionCommand(Long accountId, String password) {}
