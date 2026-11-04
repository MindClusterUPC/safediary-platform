package com.mindcluster.safediary.iam.interfaces.acl.internal;

import com.mindcluster.safediary.iam.application.commandservices.ConsentCommandService;
import com.mindcluster.safediary.iam.application.internal.outboundservices.hashing.HashingService;
import com.mindcluster.safediary.iam.application.queryservices.AccountQueryService;
import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.commands.AuthorizeConsentedAccessCommand;
import com.mindcluster.safediary.iam.domain.model.queries.GetAccountByIdQuery;
import com.mindcluster.safediary.iam.domain.model.valueobjects.SharingScope;
import com.mindcluster.safediary.iam.domain.repositories.AccountRepository;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.model.AuthenticatedAccount;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IamContextFacadeImpl implements IamContextFacade {
    private final AccountQueryService accounts;
    private final ConsentCommandService consents;
    private final AccountRepository accountRepository;
    private final HashingService hashingService;

    private static IamAccountDto toDto(Account a) {
        return new IamAccountDto(a.getId(), a.getEmail(), a.getRole().name(), a.getStatus().name());
    }

    @Override
    public Optional<IamAccountDto> fetchAuthenticatedAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount principal)) return Optional.empty();
        return fetchAccount(principal.accountId());
    }

    @Override
    public Optional<IamAccountDto> fetchAccount(Long accountId) {
        return accounts.handle(new GetAccountByIdQuery(accountId)).map(IamContextFacadeImpl::toDto);
    }

    @Override
    public Optional<IamAccountDto> fetchAccountByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        try {
            String normalized = Account.normalizeEmail(email);
            return accountRepository.findByEmail(normalized).map(IamContextFacadeImpl::toDto);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public boolean verifyPasswordByEmail(String email, String rawPassword) {
        if (email == null || rawPassword == null) return false;
        try {
            String normalized = Account.normalizeEmail(email);
            return accountRepository.findByEmail(normalized)
                    .filter(Account::isActive)
                    .filter(Account::hasPassword)
                    .map(acc -> hashingService.matches(rawPassword, acc.getPasswordHash()))
                    .orElse(false);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public boolean existsAccountByEmail(String email) {
        if (email == null || email.isBlank()) return false;
        try {
            String normalized = Account.normalizeEmail(email);
            return accountRepository.existsByEmail(normalized);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateAccountEmail(String currentEmail, String newEmail) {
        if (currentEmail == null || newEmail == null) return false;
        try {
            String normalizedCurrent = Account.normalizeEmail(currentEmail);
            String normalizedNew = Account.normalizeEmail(newEmail);
            var accountOpt = accountRepository.findByEmail(normalizedCurrent);
            if (accountOpt.isEmpty()) return false;
            Account account = accountOpt.get();
            account.changeEmail(normalizedNew);
            accountRepository.save(account);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Optional<Long> authorizeConsentedAccess(Long patientAccountId, Long specialistAccountId, String scope, LocalDate date,
                                                   String sourceContext, String resourceType, Long resourceId) {
        SharingScope sharingScope;
        try { sharingScope = SharingScope.valueOf(scope.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException | NullPointerException ex) { return Optional.empty(); }
        return consents.handle(new AuthorizeConsentedAccessCommand(patientAccountId, specialistAccountId, sharingScope, date,
                sourceContext, resourceType, resourceId)).toOptional().map(consent -> consent.getId());
    }
}
