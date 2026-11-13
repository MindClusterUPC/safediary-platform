package com.mindcluster.safediary.cliniciandirectory.infrastructure.identity;

import com.mindcluster.safediary.cliniciandirectory.application.internal.outboundservices.iam.IamRoleClient;
import com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryException;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class IamRoleClientAdapter implements IamRoleClient {
    private final IamContextFacade iam;
    private final ObjectProvider<HttpServletRequest> requests;
    private final Environment environment;
    private final boolean enabled;
    public IamRoleClientAdapter(IamContextFacade iam, ObjectProvider<HttpServletRequest> requests, Environment environment,
            @Value("${clinician-directory.demo-identity.enabled:false}") boolean enabled) {
        this.iam = iam; this.requests = requests; this.environment = environment; this.enabled = enabled;
    }
    @Override
    public DirectoryActor currentActor() {
        var account = iam.fetchAuthenticatedAccount();
        if (account.isPresent()) {
            var authenticated = account.get();
            if (!"ACTIVE".equalsIgnoreCase(authenticated.status())) throw unauthorized("The account is not active");
            try {
                return new DirectoryActor(authenticated.accountId(),
                        DirectoryRole.valueOf(authenticated.role().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException | NullPointerException ex) {
                throw unauthorized("This account cannot access the clinician directory");
            }
        }
        if (!enabled || !environment.matchesProfiles("dev & !prod"))
            throw unauthorized("Sign in is required");
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

    private static DirectoryException unauthorized(String message) {
        return new DirectoryException(new ApplicationError("UNAUTHORIZED", message));
    }
}
