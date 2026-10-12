package com.mindcluster.safediary.iam.application.commandservices;

import com.mindcluster.safediary.iam.domain.model.aggregates.PrivateVault;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.shared.application.result.*;

public interface VaultCommandService {
    Result<PrivateVault, ApplicationError> handle(CreatePrivateVaultCommand command);
    Result<PrivateVault, ApplicationError> handle(ProtectEntryInVaultCommand command);
    Result<PrivateVault, ApplicationError> handle(UnlockVaultCommand command);
}
