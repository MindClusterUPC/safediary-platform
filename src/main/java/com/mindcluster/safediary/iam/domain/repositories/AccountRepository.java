package com.mindcluster.safediary.iam.domain.repositories;

import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.entities.*;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AuthProviderType;
import java.util.List;
import java.util.Optional;

/** Accounts with their federated providers, password reset codes and biometric unlock tokens. */
public interface AccountRepository {
    Optional<Account> findById(Long id);
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    Account save(Account account);

    Optional<Long> findAccountIdByProvider(AuthProviderType type, String externalId);
    AuthProvider saveProvider(AuthProvider provider);

    List<PasswordResetCode> findUsableResetCodes(Long accountId);
    PasswordResetCode saveResetCode(PasswordResetCode code);

    Optional<BiometricUnlockToken> findUnlockTokenByHash(String tokenHash);
    List<BiometricUnlockToken> findUnlockTokensByAccountId(Long accountId);
    BiometricUnlockToken saveUnlockToken(BiometricUnlockToken token);
}
