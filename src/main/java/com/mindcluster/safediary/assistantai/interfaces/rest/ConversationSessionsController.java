package com.mindcluster.safediary.assistantai.interfaces.rest;

import com.mindcluster.safediary.assistantai.application.commandservices.ConversationCommandService;
import com.mindcluster.safediary.assistantai.application.queryservices.ConversationQueryService;
import com.mindcluster.safediary.assistantai.domain.model.commands.CloseConversationCommand;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetActiveConversationSessionQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetConversationSessionByIdQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetSessionHistoryByAccountQuery;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.ConversationSessionResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.SendTextMessageResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.StartConversationResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.UpdateAiToneResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.transform.*;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for AssistantAI conversation sessions.
 */
@RestController
@RequestMapping(value = "/api/v1/conversation-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Conversation Sessions", description = "AssistantAI conversation endpoints")
public class ConversationSessionsController {

    private final ConversationCommandService conversationCommandService;
    private final ConversationQueryService conversationQueryService;
    private final AssistantAccountGuard accountGuard;

    public ConversationSessionsController(ConversationCommandService conversationCommandService,
                                          ConversationQueryService conversationQueryService,
                                          AssistantAccountGuard accountGuard) {
        this.conversationCommandService = conversationCommandService;
        this.conversationQueryService = conversationQueryService;
        this.accountGuard = accountGuard;
    }

    @PostMapping
    @Operation(summary = "Start a conversation session")
    public ResponseEntity<?> startConversation(@Valid @RequestBody StartConversationResource resource) {
        accountGuard.requireOwnAccount(resource.accountId());
        var result = conversationCommandService.handle(
                StartConversationCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                ConversationSessionResourceFromEntityAssembler::toResource, HttpStatus.CREATED);
    }

    @PostMapping("/{sessionId}/messages")
    @Operation(summary = "Send a text message and receive the AI reflection")
    public ResponseEntity<?> sendTextMessage(@PathVariable Long sessionId,
                                             @Valid @RequestBody SendTextMessageResource resource) {
        accountGuard.requireOwnSession(sessionId);
        var result = conversationCommandService.handle(
                SendTextMessageCommandFromResourceAssembler.toCommandFromResource(sessionId, resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                ReflectionResponseResourceFromResultAssembler::toResource, HttpStatus.CREATED);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get a conversation session by id")
    public ResponseEntity<?> getSessionById(@PathVariable Long sessionId) {
        accountGuard.requireOwnSession(sessionId);
        return conversationQueryService.handle(new GetConversationSessionByIdQuery(sessionId))
                .<ResponseEntity<?>>map(session -> ResponseEntity.ok(ConversationSessionResourceFromEntityAssembler.toResource(session)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("ConversationSession", String.valueOf(sessionId))));
    }

    @GetMapping("/active")
    @Operation(summary = "Get the active conversation session of an account")
    public ResponseEntity<?> getActiveSession(@RequestParam(name = "accountId", required = false) Long requestedAccountId) {
        var accountId = accountGuard.requireOwnAccount(requestedAccountId);
        return conversationQueryService.handle(new GetActiveConversationSessionQuery(accountId))
                .<ResponseEntity<?>>map(session -> ResponseEntity.ok(ConversationSessionResourceFromEntityAssembler.toResource(session)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("ConversationSession", "active for account " + accountId)));
    }

    @GetMapping
    @Operation(summary = "Get the conversation history of an account")
    public ResponseEntity<List<ConversationSessionResource>> getSessionHistory(@RequestParam(name = "accountId", required = false) Long requestedAccountId) {
        var accountId = accountGuard.requireOwnAccount(requestedAccountId);
        var sessions = conversationQueryService.handle(new GetSessionHistoryByAccountQuery(accountId)).stream()
                .map(ConversationSessionResourceFromEntityAssembler::toResource)
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @PutMapping("/{sessionId}/tone")
    @Operation(summary = "Change the personality tone of the AI companion")
    public ResponseEntity<?> changeTone(@PathVariable Long sessionId, @Valid @RequestBody UpdateAiToneResource resource) {
        accountGuard.requireOwnSession(sessionId);
        var result = conversationCommandService.handle(
                ChangePersonalityToneCommandFromResourceAssembler.toCommandFromResource(sessionId, resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                ConversationSessionResourceFromEntityAssembler::toResource, HttpStatus.OK);
    }

    @PatchMapping("/{sessionId}/close")
    @Operation(summary = "Close a conversation session")
    public ResponseEntity<?> closeSession(@PathVariable Long sessionId) {
        accountGuard.requireOwnSession(sessionId);
        var result = conversationCommandService.handle(new CloseConversationCommand(sessionId));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                ConversationSessionResourceFromEntityAssembler::toResource, HttpStatus.OK);
    }
}
