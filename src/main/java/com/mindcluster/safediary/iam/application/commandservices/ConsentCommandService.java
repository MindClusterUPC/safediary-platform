package com.mindcluster.safediary.iam.application.commandservices;

import com.mindcluster.safediary.iam.domain.model.aggregates.Consent;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.shared.application.result.*;

public interface ConsentCommandService {
    Result<Consent, ApplicationError> handle(GrantConsentCommand command);
    Result<Consent, ApplicationError> handle(RevokeConsentCommand command);
    /** ConsentAuthorizationService contract: validates a specialist access and always audits the decision. */
    Result<Consent, ApplicationError> handle(AuthorizeConsentedAccessCommand command);
}
