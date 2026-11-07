package com.mindcluster.safediary.assistantai.application.internal.queryservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetActiveConversationSessionQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetConversationSessionByIdQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetSessionHistoryByAccountQuery;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationQueryServiceImplTest {

    @Mock
    private ConversationSessionRepository sessionRepository;

    @InjectMocks
    private ConversationQueryServiceImpl conversationQueryService;

    @Test
    @DisplayName("handle(GetConversationSessionByIdQuery) returns session when found")
    void handle_getConversationSessionById_whenFound_returnsSession() {
        var session = new ConversationSession(100L, PersonalityTone.EMPATHIC);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        var result = conversationQueryService.handle(new GetConversationSessionByIdQuery(1L));

        assertThat(result).isPresent();
        assertThat(result.get().getAccountId()).isEqualTo(100L);
        verify(sessionRepository).findById(1L);
    }

    @Test
    @DisplayName("handle(GetConversationSessionByIdQuery) returns empty when not found")
    void handle_getConversationSessionById_whenNotFound_returnsEmpty() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        var result = conversationQueryService.handle(new GetConversationSessionByIdQuery(999L));

        assertThat(result).isEmpty();
        verify(sessionRepository).findById(999L);
    }

    @Test
    @DisplayName("handle(GetActiveConversationSessionQuery) returns active session when exists")
    void handle_getActiveConversationSession_whenFound_returnsSession() {
        var session = new ConversationSession(100L, PersonalityTone.ANALYTICAL);
        when(sessionRepository.findActiveByAccountId(100L)).thenReturn(Optional.of(session));

        var result = conversationQueryService.handle(new GetActiveConversationSessionQuery(100L));

        assertThat(result).isPresent();
        assertThat(result.get().getCurrentTone()).isEqualTo(PersonalityTone.ANALYTICAL);
        verify(sessionRepository).findActiveByAccountId(100L);
    }

    @Test
    @DisplayName("handle(GetActiveConversationSessionQuery) returns empty when no active session")
    void handle_getActiveConversationSession_whenNotFound_returnsEmpty() {
        when(sessionRepository.findActiveByAccountId(200L)).thenReturn(Optional.empty());

        var result = conversationQueryService.handle(new GetActiveConversationSessionQuery(200L));

        assertThat(result).isEmpty();
        verify(sessionRepository).findActiveByAccountId(200L);
    }

    @Test
    @DisplayName("handle(GetSessionHistoryByAccountQuery) returns list of sessions for account")
    void handle_getSessionHistoryByAccount_returnsSessionsList() {
        var session1 = new ConversationSession(100L, PersonalityTone.EMPATHIC);
        var session2 = new ConversationSession(100L, PersonalityTone.REFLECTIVE);
        when(sessionRepository.findAllByAccountId(100L)).thenReturn(List.of(session1, session2));

        var result = conversationQueryService.handle(new GetSessionHistoryByAccountQuery(100L));

        assertThat(result).hasSize(2);
        verify(sessionRepository).findAllByAccountId(100L);
    }
}
