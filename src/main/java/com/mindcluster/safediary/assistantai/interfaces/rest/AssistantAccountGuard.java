package com.mindcluster.safediary.assistantai.interfaces.rest;

import com.mindcluster.safediary.assistantai.application.queryservices.ConversationQueryService;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetConversationSessionByIdQuery;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Restricts AssistantAI endpoints to the account of the bearer token. Conversations, risk assessments and clinical
 * summaries are private emotional data, so an account id or session id sent by the client is only accepted when it
 * belongs to that account.
 */
@Component
public class AssistantAccountGuard {

    private final IamContextFacade iamContextFacade;
    private final ConversationQueryService conversationQueryService;

    public AssistantAccountGuard(IamContextFacade iamContextFacade, ConversationQueryService conversationQueryService) {
        this.iamContextFacade = iamContextFacade;
        this.conversationQueryService = conversationQueryService;
    }

    public Long currentAccountId() {
        return iamContextFacade.fetchAuthenticatedAccount()
                .map(IamAccountDto::accountId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to use the assistant"));
    }

    /** Returns the current account id, rejecting a different account id sent by the client. */
    public Long requireOwnAccount(Long requestedAccountId) {
        var accountId = currentAccountId();
        if (requestedAccountId != null && !requestedAccountId.equals(accountId)) {
            throw new AccessDeniedException("The requested account does not belong to the signed-in user");
        }
        return accountId;
    }

    /** Rejects a conversation session of another account; an unknown session is left to the caller's 404. */
    public void requireOwnSession(Long sessionId) {
        var accountId = currentAccountId();
        conversationQueryService.handle(new GetConversationSessionByIdQuery(sessionId))
                .filter(session -> !session.getAccountId().equals(accountId))
                .ifPresent(session -> {
                    throw new AccessDeniedException("The conversation does not belong to the signed-in user");
                });
    }
}
