package com.mindcluster.safediary.cliniciandirectory.infrastructure.identity;

import com.mindcluster.safediary.cliniciandirectory.application.internal.outboundservices.iam.IamRoleClient;
import com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryException;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class DevelopmentIamRoleClient implements IamRoleClient {
    private final ObjectProvider<HttpServletRequest> requests;
    private final Environment environment;
    private final boolean enabled;
    public DevelopmentIamRoleClient(ObjectProvider<HttpServletRequest> requests, Environment environment,
            @Value("${clinician-directory.demo-identity.enabled:false}") boolean enabled) {
        this.requests = requests; this.environment = environment; this.enabled = enabled;
    }
    public DirectoryActor currentActor() {
        if (!enabled || !environment.matchesProfiles("dev & !prod"))
            throw new DirectoryException(new ApplicationError("UNAUTHORIZED", "Clinician Directory requires an IAM integration"));
        var request = requests.getIfAvailable();
        if (request == null || request.getHeader("X-Account-Id") == null || request.getHeader("X-Role") == null)
            throw new DirectoryException(new ApplicationError("UNAUTHORIZED", "Development requires X-Account-Id and X-Role"));
        try {
            return new DirectoryActor(Long.valueOf(request.getHeader("X-Account-Id")),
                    DirectoryRole.valueOf(request.getHeader("X-Role").toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new DirectoryException(new ApplicationError("UNAUTHORIZED", "Invalid development identity"));
        }
    }
}
