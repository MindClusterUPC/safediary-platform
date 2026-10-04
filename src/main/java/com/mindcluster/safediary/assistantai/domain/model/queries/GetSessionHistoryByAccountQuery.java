package com.mindcluster.safediary.assistantai.domain.model.queries;

/**
 * Gets all sessions of an account, newest first.
 */
public record GetSessionHistoryByAccountQuery(Long accountId) {
}
