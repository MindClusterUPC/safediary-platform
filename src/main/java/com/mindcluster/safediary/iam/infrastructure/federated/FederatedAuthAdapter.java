package com.mindcluster.safediary.iam.infrastructure.federated;

import com.mindcluster.safediary.iam.application.internal.outboundservices.federated.FederatedIdentityClient;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AuthProviderType;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.iam.application.internal.IamException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.ProtectedHeader;
import io.jsonwebtoken.security.Jwks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Validates Google Sign-In and Sign in with Apple ID tokens. Google tokens are checked with Google's tokeninfo
 * endpoint; Apple tokens are verified locally with Apple's public keys. Both must be issued for one of the
 * configured client IDs (GOOGLE_CLIENT_IDS, APPLE_CLIENT_IDS).
 */
@Component
public class FederatedAuthAdapter implements FederatedIdentityClient {
    private static final String GOOGLE_TOKEN_INFO = "https://oauth2.googleapis.com/tokeninfo?id_token={token}";
    private static final String APPLE_KEYS = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final Set<String> GOOGLE_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");
    private static final Duration APPLE_KEYS_CACHE = Duration.ofHours(6);

    private final RestClient http = RestClient.create();
    private final Set<String> googleClientIds;
    private final Set<String> appleClientIds;
    private Map<String, Key> appleKeys = Map.of();
    private Instant appleKeysLoadedAt = Instant.EPOCH;

    public FederatedAuthAdapter(@Value("${iam.federated.google.client-ids:}") String googleClientIds,
                                @Value("${iam.federated.apple.client-ids:}") String appleClientIds) {
        this.googleClientIds = split(googleClientIds);
        this.appleClientIds = split(appleClientIds);
    }

    private static Set<String> split(String values) {
        var result = new HashSet<String>();
        for (var value : values.split(",")) if (!value.isBlank()) result.add(value.trim());
        return Set.copyOf(result);
    }
    private static IamException notConfigured(AuthProviderType provider) {
        return new IamException(ApplicationError.businessRuleViolation("federated-sign-in",
                provider + " sign-in is not configured on this server"));
    }

    public Optional<FederatedIdentity> verify(AuthProviderType provider, String idToken) {
        return switch (provider) {
            case GOOGLE -> verifyGoogle(idToken);
            case APPLE -> verifyApple(idToken);
            case EMAIL -> Optional.empty();
        };
    }

    @SuppressWarnings("unchecked")
    private Optional<FederatedIdentity> verifyGoogle(String idToken) {
        if (googleClientIds.isEmpty()) throw notConfigured(AuthProviderType.GOOGLE);
        try {
            Map<String, Object> info = http.get().uri(GOOGLE_TOKEN_INFO, idToken).retrieve().body(Map.class);
            if (info == null || !googleClientIds.contains(String.valueOf(info.get("aud")))
                    || !GOOGLE_ISSUERS.contains(String.valueOf(info.get("iss"))) || info.get("sub") == null)
                return Optional.empty();
            return Optional.of(new FederatedIdentity(String.valueOf(info.get("sub")), (String) info.get("email"),
                    "true".equals(String.valueOf(info.get("email_verified")))));
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    private Optional<FederatedIdentity> verifyApple(String idToken) {
        if (appleClientIds.isEmpty()) throw notConfigured(AuthProviderType.APPLE);
        try {
            var claims = Jwts.parser().keyLocator(new LocatorAdapter<Key>() {
                @Override protected Key locate(ProtectedHeader header) { return appleKey(header.getKeyId()); }
            }).requireIssuer(APPLE_ISSUER).build().parseSignedClaims(idToken).getPayload();
            if (claims.getAudience() == null || claims.getAudience().stream().noneMatch(appleClientIds::contains))
                return Optional.empty();
            var verified = claims.get("email_verified");
            return Optional.of(new FederatedIdentity(claims.getSubject(), claims.get("email", String.class),
                    verified != null && "true".equals(String.valueOf(verified))));
        } catch (JwtException | IllegalArgumentException | RestClientException ex) {
            return Optional.empty();
        }
    }

    private synchronized Key appleKey(String keyId) {
        if (!appleKeys.containsKey(keyId) || Instant.now().isAfter(appleKeysLoadedAt.plus(APPLE_KEYS_CACHE))) {
            var keySet = Jwks.setParser().build().parse(http.get().uri(APPLE_KEYS).retrieve().body(String.class));
            var keys = new HashMap<String, Key>();
            keySet.getKeys().forEach(jwk -> keys.put(jwk.getId(), jwk.toKey()));
            appleKeys = Map.copyOf(keys);
            appleKeysLoadedAt = Instant.now();
        }
        var key = appleKeys.get(keyId);
        if (key == null) throw new JwtException("Unknown Apple signing key");
        return key;
    }
}
