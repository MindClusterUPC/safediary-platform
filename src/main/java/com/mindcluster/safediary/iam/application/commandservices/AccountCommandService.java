package com.mindcluster.safediary.iam.application.commandservices;

import com.mindcluster.safediary.iam.application.commandservices.dto.*;
import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.shared.application.result.*;

public interface AccountCommandService {
    Result<AuthenticatedAccountDto, ApplicationError> handle(RegisterAccountCommand command);
    Result<AuthenticatedAccountDto, ApplicationError> handle(SignInCommand command);
    Result<AuthenticatedAccountDto, ApplicationError> handle(AuthenticateWithProviderCommand command);
    Result<AuthenticatedAccountDto, ApplicationError> handle(SignInWithBiometricTokenCommand command);
    Result<BiometricUnlockDto, ApplicationError> handle(EnableBiometricUnlockCommand command);
    Result<Void, ApplicationError> handle(RequestPasswordResetCommand command);
    Result<Void, ApplicationError> handle(VerifyPasswordResetCodeCommand command);
    Result<Void, ApplicationError> handle(ResetPasswordCommand command);
    Result<Account, ApplicationError> handle(RequestAccountDeletionCommand command);
}
