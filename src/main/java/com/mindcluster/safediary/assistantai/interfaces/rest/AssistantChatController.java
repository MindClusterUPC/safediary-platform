package com.mindcluster.safediary.assistantai.interfaces.rest;

import com.mindcluster.safediary.assistantai.application.commandservices.ConversationCommandService;
import com.mindcluster.safediary.assistantai.domain.model.commands.SendChatPromptCommand;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.AssistantChatRequestResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.transform.AssistantChatResponseResourceFromResultAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Chat endpoint with the contract already used by SafeDiary-Mobile (RetrofitAssistantResponder).
 * <p>
 * Until IAM exists, every request is attributed to the configured demo account.
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/assistant", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Assistant Chat", description = "Mobile-compatible chat endpoint")
public class AssistantChatController {

    private final ConversationCommandService conversationCommandService;
    private final Long demoAccountId;

    public AssistantChatController(ConversationCommandService conversationCommandService,
                                   @Value("${assistantai.demo-account-id:1}") Long demoAccountId) {
        this.conversationCommandService = conversationCommandService;
        this.demoAccountId = demoAccountId;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a prompt (contract used by SafeDiary-Mobile)")
    public ResponseEntity<?> chat(@Valid @RequestBody AssistantChatRequestResource resource) {
        var command = new SendChatPromptCommand(demoAccountId, resource.conversationId(), resource.prompt(),
                resource.locale());
        var result = conversationCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                AssistantChatResponseResourceFromResultAssembler::toResource, HttpStatus.OK);
    }
}
