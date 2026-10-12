package com.mindcluster.safediary.iam.application.internal.outboundservices.tokens;

import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AccountRole;
import java.time.Instant;
import java.util.Optional;

/** Issues and validates the bearer tokens used by every bounded context, plus opaque device tokens. */
public interface TokenService {
    record IssuedToken(String value, Instant expiresAt) {}
    record AccessTokenClaims(Long accountId, String email, AccountRole role) {}

    IssuedToken issueAccessToken(Account account);
    Optional<AccessTokenClaims> readAccessToken(String token);

    /** Random, URL-safe token for device credentials such as the biometric unlock token. */
    String generateOpaqueToken();
    /** Deterministic SHA-256 hash so an opaque token can be looked up without storing it. */
    String hashOpaqueToken(String token);
}
