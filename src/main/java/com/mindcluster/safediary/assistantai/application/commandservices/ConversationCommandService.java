package com.mindcluster.safediary.assistantai.application.commandservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.commands.*;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

/**
 * Application service contract for conversation commands.
 */
public interface ConversationCommandService {

    Result<ConversationSession, ApplicationError> handle(StartConversationCommand command);

    Result<ReflectionResult, ApplicationError> handle(SendTextMessageCommand command);

    Result<ReflectionResult, ApplicationError> handle(SendChatPromptCommand command);

    Result<ConversationSession, ApplicationError> handle(ChangePersonalityToneCommand command);

    Result<ConversationSession, ApplicationError> handle(RenameConversationCommand command);

    Result<Void, ApplicationError> handle(DeleteConversationCommand command);

    Result<ReflectionResult, ApplicationError> handle(EditUserMessageCommand command);

    Result<ConversationSession, ApplicationError> handle(CloseConversationCommand command);
}
