package com.mindcluster.safediary.carescheduling.infrastructure.acl;

import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingException;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Locale;
import java.util.Optional;

/**
 * Temporary IAM adapter, only in the dev profile: identity from X-Account-Id and X-Role, and consent from
 * X-Consent-Ref. Outside dev there is no identity and no consent, so nothing is shared by default.
 */
@Component
public class DevelopmentIamClient implements IamClient {
    private final ObjectProvider<HttpServletRequest> requests;
    private final Environment environment;
    private final boolean enabled;

    public DevelopmentIamClient(ObjectProvider<HttpServletRequest> requests, Environment environment,
            @Value("${care-scheduling.demo-identity.enabled:false}") boolean enabled) {
        this.requests = requests; this.environment = environment; this.enabled = enabled;
    }

    private boolean active() { return enabled && environment.matchesProfiles("dev & !prod"); }

    public CareActor currentActor() {
        if (!active()) throw new CareSchedulingException(new ApplicationError("UNAUTHORIZED", "Care Scheduling requires an IAM integration"));
        var request = requests.getIfAvailable();
        if (request == null || request.getHeader("X-Account-Id") == null || request.getHeader("X-Role") == null)
            throw new CareSchedulingException(new ApplicationError("UNAUTHORIZED", "Development requires X-Account-Id and X-Role"));
        try {
            return new CareActor(Long.valueOf(request.getHeader("X-Account-Id")),
                    CareRole.valueOf(request.getHeader("X-Role").toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new CareSchedulingException(new ApplicationError("UNAUTHORIZED", "Invalid development identity"));
        }
    }

    public Optional<String> findActiveSummaryConsent(Long patientAccountId, Long clinicianAccountId) {
        var request = active() ? requests.getIfAvailable() : null;
        var consentRef = request == null ? null : request.getHeader("X-Consent-Ref");
        if (consentRef == null || consentRef.isBlank() || consentRef.length() > 128) return Optional.empty();
        return Optional.of(consentRef.trim());
    }
}
