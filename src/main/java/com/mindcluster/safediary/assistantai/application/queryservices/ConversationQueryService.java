package com.mindcluster.safediary.assistantai.application.queryservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetActiveConversationSessionQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetConversationSessionByIdQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetSessionHistoryByAccountQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for conversation queries.
 */
public interface ConversationQueryService {

    Optional<ConversationSession> handle(GetConversationSessionByIdQuery query);

    Optional<ConversationSession> handle(GetActiveConversationSessionQuery query);

    List<ConversationSession> handle(GetSessionHistoryByAccountQuery query);
}
