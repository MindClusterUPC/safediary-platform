package com.mindcluster.safediary.assistantai.application.internal.queryservices;

import com.mindcluster.safediary.assistantai.application.queryservices.ConversationQueryService;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetActiveConversationSessionQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetConversationSessionByIdQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetSessionHistoryByAccountQuery;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Conversation query service implementation.
 */
@Service
public class ConversationQueryServiceImpl implements ConversationQueryService {

    private final ConversationSessionRepository sessionRepository;

    public ConversationQueryServiceImpl(ConversationSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Optional<ConversationSession> handle(GetConversationSessionByIdQuery query) {
        return sessionRepository.findById(query.sessionId());
    }

    @Override
    public Optional<ConversationSession> handle(GetActiveConversationSessionQuery query) {
        return sessionRepository.findActiveByAccountId(query.accountId());
    }

    @Override
    public List<ConversationSession> handle(GetSessionHistoryByAccountQuery query) {
        return sessionRepository.findAllByAccountId(query.accountId());
    }
}
