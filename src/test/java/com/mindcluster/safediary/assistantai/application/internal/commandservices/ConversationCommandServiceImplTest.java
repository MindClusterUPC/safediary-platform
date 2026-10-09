package com.mindcluster.safediary.assistantai.application.internal.commandservices;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.crisis.CrisisHotlineDirectory;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.AssistantLanguageModel;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmUnavailableException;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.commands.CloseConversationCommand;
import com.mindcluster.safediary.assistantai.domain.model.commands.SendTextMessageCommand;
import com.mindcluster.safediary.assistantai.domain.model.commands.StartConversationCommand;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskEvaluation;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import com.mindcluster.safediary.assistantai.domain.repositories.RiskAssessmentRepository;
import com.mindcluster.safediary.assistantai.domain.services.CognitiveDistortionService;
import com.mindcluster.safediary.assistantai.domain.services.EmotionClassifierService;
import com.mindcluster.safediary.assistantai.domain.services.RiskPolicyService;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationCommandServiceImplTest {

    @Mock
    private ConversationSessionRepository sessionRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private AssistantLanguageModel languageModel;

    @Mock
    private CrisisHotlineDirectory crisisHotlineDirectory;

    @Mock
    private RiskPolicyService riskPolicyService;

    @Mock
    private EmotionClassifierService emotionClassifierService;

    @Mock
    private CognitiveDistortionService cognitiveDistortionService;

    @InjectMocks
    private ConversationCommandServiceImpl conversationCommandService;

    @Test
    @DisplayName("handle(StartConversationCommand) creates and returns new session when no active session exists")
    void handle_startConversation_whenNoActiveSession_savesAndReturnsSession() {
        var command = new StartConversationCommand(100L, PersonalityTone.EMPATHIC);

        when(sessionRepository.findActiveByAccountId(100L)).thenReturn(Optional.empty());
        when(sessionRepository.save(any(ConversationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = conversationCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var session = ((Result.Success<ConversationSession, ?>) result).value();
        assertThat(session.getAccountId()).isEqualTo(100L);
        assertThat(session.getCurrentTone()).isEqualTo(PersonalityTone.EMPATHIC);
        verify(sessionRepository).save(any(ConversationSession.class));
    }

    @Test
    @DisplayName("handle(StartConversationCommand) returns conflict when active session already exists")
    void handle_startConversation_whenActiveSessionAlreadyExists_returnsConflictError() {
        var activeSession = new ConversationSession(100L, PersonalityTone.EMPATHIC);
        when(sessionRepository.findActiveByAccountId(100L)).thenReturn(Optional.of(activeSession));

        var command = new StartConversationCommand(100L, PersonalityTone.ANALYTICAL);
        var result = conversationCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("CONVERSATIONSESSION_CONFLICT");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(SendTextMessageCommand) returns not found when session does not exist")
    void handle_sendTextMessage_whenSessionNotFound_returnsNotFoundError() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        var command = new SendTextMessageCommand(999L, "Hola", "es");
        var result = conversationCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("CONVERSATIONSESSION_NOT_FOUND");
        verify(languageModel, never()).generateReflection(any());
    }

    @Test
    @DisplayName("handle(SendTextMessageCommand) activates crisis protocol and never invokes language model when risk is critical")
    void handle_sendTextMessage_whenCrisisRiskDetected_activatesCrisisProtocolWithoutCallingLanguageModel() {
        var session = new ConversationSession(1L, 100L, java.time.Instant.now(), null, SessionStatus.ACTIVE, PersonalityTone.EMPATHIC, new java.util.ArrayList<>());
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        var criticalRisk = new RiskEvaluation(RiskLevel.CRITICAL, 1.0, List.of("matarme"));
        when(riskPolicyService.evaluate(anyString(), isNull())).thenReturn(criticalRisk);
        when(riskPolicyService.requiresCrisisProtocol(criticalRisk)).thenReturn(true);
        when(crisisHotlineDirectory.findAll()).thenReturn(List.of(new CrisisHotline("Línea 113", "113", "MINSA")));
        when(sessionRepository.save(any(ConversationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new SendTextMessageCommand(1L, "No quiero seguir viviendo", "es");
        var result = conversationCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var reflection = ((Result.Success<?, ?>) result).value();
        assertThat(reflection).isNotNull();

        // Safety verification: LLM must NEVER be called on high/critical risk
        verify(languageModel, never()).generateReflection(any());
        verify(riskAssessmentRepository).save(any());
        assertThat(session.isInCrisis()).isTrue();
    }

    @Test
    @DisplayName("handle(SendTextMessageCommand) returns ASSISTANT_UNAVAILABLE when language model throws LlmUnavailableException")
    void handle_sendTextMessage_whenLanguageModelUnavailable_returnsAssistantUnavailableError() {
        var session = new ConversationSession(100L, PersonalityTone.EMPATHIC);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        var lowRisk = new RiskEvaluation(RiskLevel.LOW, 0.1, List.of());
        when(riskPolicyService.evaluate(anyString(), isNull())).thenReturn(lowRisk);
        when(riskPolicyService.requiresCrisisProtocol(lowRisk)).thenReturn(false);
        when(languageModel.generateReflection(any())).thenThrow(new LlmUnavailableException("Service unreachable", new RuntimeException()));

        var command = new SendTextMessageCommand(1L, "Hoy fue un dia productivo", "es");
        var result = conversationCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("ASSISTANT_UNAVAILABLE");
    }

    @Test
    @DisplayName("handle(CloseConversationCommand) closes session and saves state")
    void handle_closeConversation_whenSessionExists_closesAndReturnsSession() {
        var session = new ConversationSession(100L, PersonalityTone.EMPATHIC);
        when(sessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ConversationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new CloseConversationCommand(5L);
        var result = conversationCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var closed = ((Result.Success<ConversationSession, ?>) result).value();
        assertThat(closed.getStatus()).isEqualTo(SessionStatus.CLOSED);
        verify(sessionRepository).save(session);
    }
}
