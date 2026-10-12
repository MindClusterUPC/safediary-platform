package com.mindcluster.safediary.iam.infrastructure.authorization.sfs.configuration;

import com.mindcluster.safediary.iam.application.internal.outboundservices.tokens.TokenService;
import com.mindcluster.safediary.iam.domain.repositories.AccountRepository;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.pipeline.SecurityErrorWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. IAM's own private endpoints require a bearer token; the rest of the API keeps its
 * current access rules and receives the verified identity from the bearer filter whenever a token is sent.
 */
@Configuration
public class WebSecurityConfiguration {
    private static final String[] IAM_PRIVATE_ENDPOINTS = {
            "/api/v1/accounts/**", "/api/v1/consents/**", "/api/v1/consent-scopes/**", "/api/v1/vault/**", "/api/v1/audit-events/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokens, AccountRepository accounts) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> SecurityErrorWriter.write(response,
                                HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Sign in and send the access token as a Bearer token"))
                        .accessDeniedHandler((request, response, ex) -> SecurityErrorWriter.write(response,
                                HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Access to this resource is not allowed")))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(IAM_PRIVATE_ENDPOINTS).authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new BearerAuthorizationRequestFilter(tokens, accounts), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Accounts authenticate only through IAM tokens; this also stops Spring Boot from creating a default user. */
    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> { throw new ProviderNotFoundException("SafeDiary uses IAM bearer tokens"); };
    }
}
