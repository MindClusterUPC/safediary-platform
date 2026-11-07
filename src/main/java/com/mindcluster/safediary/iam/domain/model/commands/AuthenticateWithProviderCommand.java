package com.mindcluster.safediary.iam.domain.model.commands;

import com.mindcluster.safediary.iam.domain.model.valueobjects.*;

public record AuthenticateWithProviderCommand(AuthProviderType provider, String idToken, AccountRole role) {}
