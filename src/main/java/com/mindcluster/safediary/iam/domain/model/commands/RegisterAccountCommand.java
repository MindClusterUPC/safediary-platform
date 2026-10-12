package com.mindcluster.safediary.iam.domain.model.commands;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;

public record RegisterAccountCommand(String email, String password, AccountRole role) {}
