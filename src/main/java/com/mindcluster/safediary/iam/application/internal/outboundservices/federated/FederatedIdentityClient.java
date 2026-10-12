package com.mindcluster.safediary.iam.application.internal.outboundservices.federated;

import com.mindcluster.safediary.iam.domain.model.valueobjects.AuthProviderType;
import java.util.Optional;

/** Validates the ID token returned by Google Sign-In or Sign in with Apple on the device. */
public interface FederatedIdentityClient {
    record FederatedIdentity(String externalId, String email, boolean emailVerified) {}

    /** Empty when the token is invalid, expired or issued for another application. */
    Optional<FederatedIdentity> verify(AuthProviderType provider, String idToken);
}
