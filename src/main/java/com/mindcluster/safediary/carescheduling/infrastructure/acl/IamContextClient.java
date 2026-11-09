package com.mindcluster.safediary.carescheduling.infrastructure.acl;

import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingException;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.CareActor;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.CareRole;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;

/**
 * IAM adapter (Open Host Service + Anticorruption Layer). The actor is the account authenticated by the bearer token,
 * and the consent to read an emotional summary is validated in IAM at read time, never cached.
 * Only in the dev profile, and when care-scheduling.demo-identity.enabled is on, a request without an authenticated
 * IAM account can still identify itself with the X-Account-Id and X-Role headers for local testing.
 */
@Component
public class IamContextClient implements IamClient {
    private static final String SUMMARY_SCOPE = "AI_CLINICAL_SUMMARIES";
    private static final String SOURCE_CONTEXT = "CARE_SCHEDULING";
    private static final String RESOURCE_TYPE = "APPOINTMENT";

    private final IamContextFacade iam;
    private final ObjectProvider<HttpServletRequest> requests;
    private final Environment environment;
    private final boolean developmentIdentity;
    private final ZoneId zone;

    public IamContextClient(IamContextFacade iam, ObjectProvider<HttpServletRequest> requests, Environment environment,
            @Value("${care-scheduling.demo-identity.enabled:false}") boolean developmentIdentity,
            @Value("${care-scheduling.zone:America/Lima}") String zoneId) {
        this.iam = iam; this.requests = requests; this.environment = environment;
        this.developmentIdentity = developmentIdentity; this.zone = ZoneId.of(zoneId);
    }

    @Override
    public CareActor currentActor() {
        var account = iam.fetchAuthenticatedAccount();
        if (account.isPresent()) {
            var authenticated = account.get();
            if (!"ACTIVE".equalsIgnoreCase(authenticated.status())) throw unauthorized("The account is not active");
            return new CareActor(authenticated.accountId(), role(authenticated.role()));
        }
        if (developmentIdentity && environment.matchesProfiles("dev & !prod")) return developmentActor();
        throw unauthorized("Sign in is required");
    }

    @Override
    public Optional<String> findActiveSummaryConsent(Long patientAccountId, Long clinicianAccountId, Long appointmentId) {
        return iam.authorizeConsentedAccess(patientAccountId, clinicianAccountId, SUMMARY_SCOPE, LocalDate.now(zone),
                SOURCE_CONTEXT, RESOURCE_TYPE, appointmentId).map(String::valueOf);
    }

    private CareActor developmentActor() {
        var request = requests.getIfAvailable();
        if (request == null || request.getHeader("X-Account-Id") == null || request.getHeader("X-Role") == null)
            throw unauthorized("Sign in is required");
        try {
            return new CareActor(Long.valueOf(request.getHeader("X-Account-Id")), role(request.getHeader("X-Role")));
        } catch (IllegalArgumentException ex) {
            throw unauthorized("Invalid development identity");
        }
    }

    private static CareRole role(String value) {
        try {
            return CareRole.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw unauthorized("This role cannot take part in care scheduling");
        }
    }

    private static CareSchedulingException unauthorized(String reason) {
        return new CareSchedulingException(new ApplicationError("UNAUTHORIZED", reason));
    }
}
