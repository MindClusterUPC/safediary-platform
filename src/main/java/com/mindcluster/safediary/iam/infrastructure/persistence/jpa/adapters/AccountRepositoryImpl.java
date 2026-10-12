package com.mindcluster.safediary.iam.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.entities.*;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AuthProviderType;
import com.mindcluster.safediary.iam.domain.repositories.AccountRepository;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.assemblers.IamPersistenceAssembler;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.repositories.*;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class AccountRepositoryImpl implements AccountRepository {
    private final AccountPersistenceRepository persistence;
    private final AuthProviderPersistenceRepository providers;
    private final PasswordResetCodePersistenceRepository resetCodes;
    private final BiometricUnlockTokenPersistenceRepository unlockTokens;
    private final DomainEventPublisher events;

    public Optional<Account> findById(Long id) { return persistence.findById(id).map(IamPersistenceAssembler::toDomain); }
    public Optional<Account> findByEmail(String email) { return persistence.findByEmail(email).map(IamPersistenceAssembler::toDomain); }
    public boolean existsByEmail(String email) { return persistence.existsByEmail(email); }

    @Transactional public Account save(Account account) {
        var entity = account.getId() == null ? new AccountPersistenceEntity() : persistence.findById(account.getId()).orElseThrow();
        IamPersistenceAssembler.copyToEntity(account, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(account);
        return IamPersistenceAssembler.toDomain(saved);
    }

    public Optional<Long> findAccountIdByProvider(AuthProviderType type, String externalId) {
        return providers.findByProviderTypeAndExternalId(type, externalId).map(AuthProviderPersistenceEntity::getAccountId);
    }
    @Transactional public AuthProvider saveProvider(AuthProvider provider) {
        var entity = new AuthProviderPersistenceEntity();
        IamPersistenceAssembler.copyToEntity(provider, entity);
        return IamPersistenceAssembler.toDomain(providers.saveAndFlush(entity));
    }

    public List<PasswordResetCode> findUsableResetCodes(Long accountId) {
        return resetCodes.findByAccountIdAndUsedAtIsNull(accountId).stream().map(IamPersistenceAssembler::toDomain).toList();
    }
    @Transactional public PasswordResetCode saveResetCode(PasswordResetCode code) {
        var entity = code.getId() == null ? new PasswordResetCodePersistenceEntity() : resetCodes.findById(code.getId()).orElseThrow();
        IamPersistenceAssembler.copyToEntity(code, entity);
        return IamPersistenceAssembler.toDomain(resetCodes.saveAndFlush(entity));
    }

    public Optional<BiometricUnlockToken> findUnlockTokenByHash(String tokenHash) {
        return unlockTokens.findByTokenHash(tokenHash).map(IamPersistenceAssembler::toDomain);
    }
    public List<BiometricUnlockToken> findUnlockTokensByAccountId(Long accountId) {
        return unlockTokens.findByAccountIdAndRevokedAtIsNull(accountId).stream().map(IamPersistenceAssembler::toDomain).toList();
    }
    @Transactional public BiometricUnlockToken saveUnlockToken(BiometricUnlockToken token) {
        var entity = token.getId() == null ? new BiometricUnlockTokenPersistenceEntity() : unlockTokens.findById(token.getId()).orElseThrow();
        IamPersistenceAssembler.copyToEntity(token, entity);
        return IamPersistenceAssembler.toDomain(unlockTokens.saveAndFlush(entity));
    }
}
