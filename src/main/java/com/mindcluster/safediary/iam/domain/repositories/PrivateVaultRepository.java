package com.mindcluster.safediary.iam.domain.repositories;

import com.mindcluster.safediary.iam.domain.model.aggregates.PrivateVault;
import java.util.Optional;

public interface PrivateVaultRepository {
    Optional<PrivateVault> findByAccountId(Long accountId);
    PrivateVault save(PrivateVault vault);
}
