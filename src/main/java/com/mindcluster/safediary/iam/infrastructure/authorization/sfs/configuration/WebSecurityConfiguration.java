package com.mindcluster.safediary.iam.infrastructure.authorization.sfs.configuration;

import com.mindcluster.safediary.iam.application.internal.outboundservices.tokens.TokenService;
import com.mindcluster.safediary.iam.domain.repositories.AccountRepository;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.pipeline.SecurityErrorWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. Private endpoints require a bearer token; public endpoints
 * (authentication, OpenAPI/Swagger, Scalar docs, webhooks, public directory catalog) are accessible without authentication.
 */
@Configuration
public class WebSecurityConfiguration {
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/authentication/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/scalar/**",
            "/scalar",
            "/docs/**",
            "/docs",
            "/api/v1/payments/webhooks/**",
            "/api/v1/dev/**",
            "/error",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokens, AccountRepository accounts, Environment environment) throws Exception {
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
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/clinicians", "/api/v1/clinicians/*", "/api/v1/clinicians/*/rating", "/api/v1/clinicians/*/reviews").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new BearerAuthorizationRequestFilter(tokens, accounts, environment), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Accounts authenticate only through IAM tokens; this also stops Spring Boot from creating a default user. */
    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> { throw new ProviderNotFoundException("SafeDiary uses IAM bearer tokens"); };
    }
}
