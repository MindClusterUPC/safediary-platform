package com.mindcluster.safediary.payments.application.commandservices;

import com.mindcluster.safediary.payments.application.commandservices.dto.ConnectedAccountOnboardingDto;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConnectedAccountCommand;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface ConnectedAccountCommandService {
    Result<ConnectedAccountOnboardingDto, ApplicationError> handle(CreateConnectedAccountCommand command);
}
