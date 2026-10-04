package com.mindcluster.safediary.assistantai.interfaces.rest;

import com.mindcluster.safediary.assistantai.application.commandservices.ConversationCommandService;
import com.mindcluster.safediary.assistantai.application.queryservices.ConversationQueryService;
import com.mindcluster.safediary.assistantai.application.queryservices.CrisisQueryService;
import com.mindcluster.safediary.assistantai.domain.model.commands.SendChatPromptCommand;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetConversationSessionByIdQuery;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetSessionHistoryByAccountQuery;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.AssistantChatRequestResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.AssistantConversationSummaryResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.transform.AssistantChatResponseResourceFromResultAssembler;
import com.mindcluster.safediary.assistantai.interfaces.rest.transform.AssistantConversationResourceFromEntityAssembler;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * Chat endpoints with the contract used by SafeDiary-Mobile (RetrofitAssistantResponder).
 * <p>
 * Until IAM exists, every request is attributed to the configured demo account.
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/assistant", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Assistant Chat", description = "Mobile-compatible chat endpoints")
public class AssistantChatController {

    private final ConversationCommandService conversationCommandService;
    private final ConversationQueryService conversationQueryService;
    private final CrisisQueryService crisisQueryService;
    private final Long demoAccountId;

    public AssistantChatController(ConversationCommandService conversationCommandService,
                                   ConversationQueryService conversationQueryService,
                                   CrisisQueryService crisisQueryService,
                                   @Value("${assistantai.demo-account-id:1}") Long demoAccountId) {
        this.conversationCommandService = conversationCommandService;
        this.conversationQueryService = conversationQueryService;
        this.crisisQueryService = crisisQueryService;
        this.demoAccountId = demoAccountId;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a prompt; without conversationId a new conversation is started")
    public ResponseEntity<?> chat(@Valid @RequestBody AssistantChatRequestResource resource) {
        var command = new SendChatPromptCommand(demoAccountId, resource.conversationId(), resource.prompt(),
                resource.locale());
        var result = conversationCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                AssistantChatResponseResourceFromResultAssembler::toResource, HttpStatus.OK);
    }

    @GetMapping("/conversations")
    @Operation(summary = "List the conversations of the current account, most recent activity first")
    public ResponseEntity<List<AssistantConversationSummaryResource>> listConversations() {
        var conversations = conversationQueryService.handle(new GetSessionHistoryByAccountQuery(demoAccountId)).stream()
                .filter(session -> !session.getMessages().isEmpty())
                .sorted(Comparator.comparing(AssistantConversationResourceFromEntityAssembler::lastActivityOf).reversed())
                .map(AssistantConversationResourceFromEntityAssembler::toSummaryResource)
                .toList();
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Open a conversation of the current account with all its messages")
    public ResponseEntity<?> getConversation(@PathVariable Long conversationId) {
        return conversationQueryService.handle(new GetConversationSessionByIdQuery(conversationId))
                .filter(session -> session.getAccountId().equals(demoAccountId))
                .<ResponseEntity<?>>map(session -> ResponseEntity.ok(AssistantConversationResourceFromEntityAssembler
                        .toResource(session, crisisQueryService.getCrisisHotlines())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Conversation", String.valueOf(conversationId))));
    }
}
