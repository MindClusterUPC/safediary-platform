package com.mindcluster.safediary.iam.domain.model.entities;

import com.mindcluster.safediary.iam.domain.model.valueobjects.AuthProviderType;

/** Federated provider (Google or Apple) linked to an account through its external subject identifier. */
public record AuthProvider(Long id, Long accountId, AuthProviderType type, String externalId) {
    public AuthProvider {
        if (accountId == null || type == null || type == AuthProviderType.EMAIL
                || externalId == null || externalId.isBlank() || externalId.length() > 255)
            throw new IllegalArgumentException("A federated provider requires an account, a provider and an external id");
    }
}
