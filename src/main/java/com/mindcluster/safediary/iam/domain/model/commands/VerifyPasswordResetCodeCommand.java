package com.mindcluster.safediary.iam.domain.model.commands;


public record VerifyPasswordResetCodeCommand(String email, String code) {}
