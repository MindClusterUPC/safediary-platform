package com.mindcluster.safediary.iam.interfaces.acl.internal;

import com.mindcluster.safediary.iam.application.commandservices.ConsentCommandService;
import com.mindcluster.safediary.iam.application.queryservices.AccountQueryService;
import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.commands.AuthorizeConsentedAccessCommand;
import com.mindcluster.safediary.iam.domain.model.queries.GetAccountByIdQuery;
import com.mindcluster.safediary.iam.domain.model.valueobjects.SharingScope;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.model.AuthenticatedAccount;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class IamContextFacadeImpl implements IamContextFacade {
    private final AccountQueryService accounts;
    private final ConsentCommandService consents;

    private static IamAccountDto toDto(Account a) {
        return new IamAccountDto(a.getId(), a.getEmail(), a.getRole().name(), a.getStatus().name());
    }

    public Optional<IamAccountDto> fetchAuthenticatedAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount principal)) return Optional.empty();
        return fetchAccount(principal.accountId());
    }

    public Optional<IamAccountDto> fetchAccount(Long accountId) {
        return accounts.handle(new GetAccountByIdQuery(accountId)).map(IamContextFacadeImpl::toDto);
    }

    public Optional<Long> authorizeConsentedAccess(Long patientAccountId, Long specialistAccountId, String scope, LocalDate date,
                                                   String sourceContext, String resourceType, Long resourceId) {
        SharingScope sharingScope;
        try { sharingScope = SharingScope.valueOf(scope.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException | NullPointerException ex) { return Optional.empty(); }
        return consents.handle(new AuthorizeConsentedAccessCommand(patientAccountId, specialistAccountId, sharingScope, date,
                sourceContext, resourceType, resourceId)).toOptional().map(consent -> consent.getId());
    }
}
