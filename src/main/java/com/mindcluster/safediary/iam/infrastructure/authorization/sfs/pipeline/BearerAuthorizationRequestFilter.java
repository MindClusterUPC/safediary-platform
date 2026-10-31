package com.mindcluster.safediary.iam.infrastructure.authorization.sfs.pipeline;

import com.mindcluster.safediary.iam.application.internal.outboundservices.tokens.TokenService;
import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AccountRole;
import com.mindcluster.safediary.iam.domain.repositories.AccountRepository;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.model.AuthenticatedAccount;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.*;

/**
 * Validates "Authorization: Bearer" tokens issued by IAM. Besides authenticating the request, it publishes the
 * verified identity in the X-Account-Id, X-Role and X-Patient-Account-Id headers that the other bounded contexts
 * already read, replacing any value sent by the client so the identity cannot be spoofed.
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {
    private static final String PUBLIC_AUTHENTICATION_PATH = "/api/v1/authentication/";
    private final TokenService tokens;
    private final AccountRepository accounts;
    private final Environment environment;

    public BearerAuthorizationRequestFilter(TokenService tokens, AccountRepository accounts, Environment environment) {
        this.tokens = tokens;
        this.accounts = accounts;
        this.environment = environment;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath))
                ? uri.substring(contextPath.length())
                : uri;

        return path.startsWith(PUBLIC_AUTHENTICATION_PATH)
                || path.startsWith("/api/v1/payments/webhooks/")
                || path.equals("/api/v1/payments/subscriptions/success")
                || path.equals("/api/v1/payments/subscriptions/cancel")
                || path.equals("/api/v1/payments/connect/refresh")
                || path.equals("/api/v1/payments/connect/return")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/scalar")
                || path.startsWith("/docs")
                || path.startsWith("/api/v1/dev/")
                || path.equals("/error")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            var account = tokens.readAccessToken(header.substring(7).trim())
                    .flatMap(claims -> accounts.findById(claims.accountId())).filter(Account::isActive);
            if (account.isEmpty()) {
                SecurityErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                        "The access token is invalid, expired or belongs to an inactive account");
                return;
            }
            var principal = new AuthenticatedAccount(account.get().getId(), account.get().getEmail(), account.get().getRole());
            SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))));
            chain.doFilter(new IdentityHeadersRequest(request, principal), response);
            return;
        }

        // Support for test/dev environments passing direct development headers
        if (environment != null && environment.matchesProfiles("dev & !prod")) {
            var devAccountId = request.getHeader("X-Account-Id");
            var devRole = request.getHeader("X-Role");
            if (devAccountId != null && devRole != null) {
                try {
                    long accId = Long.parseLong(devAccountId);
                    var role = AccountRole.valueOf(devRole.toUpperCase(Locale.ROOT));
                    var principal = new AuthenticatedAccount(accId, "dev-" + accId + "@safediary.internal", role);
                    SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
                } catch (Exception ignored) {
                }
            }
        }

        chain.doFilter(request, response);
    }

    /** Request view whose identity headers come from the validated token. */
    private static final class IdentityHeadersRequest extends HttpServletRequestWrapper {
        private final Map<String, String> identity = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        IdentityHeadersRequest(HttpServletRequest request, AuthenticatedAccount account) {
            super(request);
            identity.put("X-Account-Id", String.valueOf(account.accountId()));
            identity.put("X-Role", account.role().name());
            if (account.role() == AccountRole.PATIENT) identity.put("X-Patient-Account-Id", String.valueOf(account.accountId()));
        }

        @Override public String getHeader(String name) {
            return identity.containsKey(name) ? identity.get(name) : super.getHeader(name);
        }
        @Override public Enumeration<String> getHeaders(String name) {
            return identity.containsKey(name) ? Collections.enumeration(List.of(identity.get(name))) : super.getHeaders(name);
        }
        @Override public Enumeration<String> getHeaderNames() {
            var names = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
            names.addAll(identity.keySet());
            return Collections.enumeration(names);
        }
    }
}
