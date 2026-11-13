package com.mindcluster.safediary.payments.interfaces.rest;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Restricts patient payment endpoints to the account of the bearer token: subscriptions, payment history and saved
 * payment methods are only returned or changed for the signed-in account.
 */
@Component
public class PaymentsAccountGuard {

    private final IamContextFacade iamContextFacade;

    public PaymentsAccountGuard(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    /** Returns the current account id, rejecting a different account id sent by the client. */
    public Long requireOwnAccount(Long requestedAccountId) {
        return requireOwnAccountWithRole(requestedAccountId, null);
    }

    /**
     * Returns the current account id after verifying both the expected IAM role (when provided)
     * and that any client-supplied account id matches the signed-in account.
     */
    public Long requireOwnAccountWithRole(Long requestedAccountId, String requiredRole) {
        var account = iamContextFacade.fetchAuthenticatedAccount()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to manage your payments"));
        if (requiredRole != null && !requiredRole.equalsIgnoreCase(account.role())) {
            throw new AccessDeniedException("Operation restricted to " + requiredRole + " role");
        }
        if (requestedAccountId != null && !requestedAccountId.equals(account.accountId())) {
            throw new AccessDeniedException("The payment data does not belong to the signed-in user");
        }
        return account.accountId();
    }
}
