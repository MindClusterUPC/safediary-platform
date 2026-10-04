package com.mindcluster.safediary.assistantai.domain.model.queries;

/**
 * Gets the active (or in crisis) session of an account.
 */
public record GetActiveConversationSessionQuery(Long accountId) {
}
