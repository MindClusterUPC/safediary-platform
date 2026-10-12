package com.mindcluster.safediary.iam.infrastructure.tokens.jwt;

import com.mindcluster.safediary.iam.application.internal.outboundservices.tokens.TokenService;
import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AccountRole;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;

/**
 * HS256 access tokens. The signing secret comes from IAM_JWT_SECRET and never from the source code (TS-002):
 * production refuses to start without it; other profiles use a random key that lives until the restart.
 */
@Service
public class JwtTokenService implements TokenService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String ISSUER = "safediary-iam";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKey key;
    private final Duration validity;

    public JwtTokenService(@Value("${iam.jwt.secret:}") String secret,
                           @Value("${iam.jwt.expiration-minutes:1440}") long expirationMinutes, Environment environment) {
        this.key = Keys.hmacShaKeyFor(resolveSecret(secret, environment));
        this.validity = Duration.ofMinutes(expirationMinutes);
    }

    private static byte[] resolveSecret(String secret, Environment environment) {
        if (secret != null && !secret.isBlank()) {
            var bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) throw new IllegalStateException("IAM_JWT_SECRET must have at least 32 characters");
            return bytes;
        }
        if (environment.matchesProfiles("prod")) throw new IllegalStateException("IAM_JWT_SECRET is required in production");
        LOGGER.warn("IAM_JWT_SECRET is not set; using a temporary signing key, tokens stop working after a restart");
        var random = new byte[32];
        RANDOM.nextBytes(random);
        return random;
    }

    public IssuedToken issueAccessToken(Account account) {
        var now = Instant.now();
        var expiresAt = now.plus(validity);
        var token = Jwts.builder().issuer(ISSUER).subject(String.valueOf(account.getId()))
                .claim("email", account.getEmail()).claim("role", account.getRole().name())
                .issuedAt(Date.from(now)).expiration(Date.from(expiresAt)).signWith(key).compact();
        return new IssuedToken(token, expiresAt);
    }

    public Optional<AccessTokenClaims> readAccessToken(String token) {
        try {
            var claims = Jwts.parser().verifyWith(key).requireIssuer(ISSUER).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AccessTokenClaims(Long.valueOf(claims.getSubject()), claims.get("email", String.class),
                    AccountRole.valueOf(claims.get("role", String.class))));
        } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
            return Optional.empty();
        }
    }

    public String generateOpaqueToken() {
        var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashOpaqueToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
