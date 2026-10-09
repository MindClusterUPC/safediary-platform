package com.mindcluster.safediary.carescheduling.interfaces.rest.controllers;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.application.queryservices.CareSchedulingQueryService;
import com.mindcluster.safediary.carescheduling.domain.model.aggregates.ContactRequest;
import com.mindcluster.safediary.carescheduling.domain.model.commands.*;
import com.mindcluster.safediary.carescheduling.domain.model.queries.*;
import com.mindcluster.safediary.carescheduling.interfaces.rest.resources.*;
import com.mindcluster.safediary.carescheduling.interfaces.rest.transform.CareSchedulingResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/contact-requests", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Care Scheduling - Contact Requests")
public class ContactRequestsController {
    private final CareSchedulingCommandService commands;
    private final CareSchedulingQueryService queries;
    private final IamClient iam;

    private ContactRequestResource withLastMessage(ContactRequest r) {
        return CareSchedulingResourceAssembler.contactRequest(r, queries.latestMessage(r.getId()).orElse(null));
    }

    @PostMapping @Operation(summary="Request contact with a verified clinician; an open chat with the same clinician is reused")
    public ResponseEntity<?> send(@Valid @RequestBody SendContactRequestResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new SendContactRequestCommand(iam.currentActor(),
                r.clinicianId(), r.message())), this::withLastMessage, HttpStatus.CREATED);
    }
    @GetMapping @Operation(summary="Your conversations: sent requests for patients, received requests for psychologists")
    public List<ContactRequestResource> list() {
        return queries.handle(new GetContactRequestsQuery(iam.currentActor())).stream().map(this::withLastMessage).toList();
    }
    @PatchMapping("/{id}/decision") @Operation(summary="Psychologist: open the chat or reject the request")
    public ResponseEntity<?> decide(@PathVariable Long id, @Valid @RequestBody RespondContactRequestResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new RespondToContactRequestCommand(
                iam.currentActor(), id, r.accept())), this::withLastMessage, HttpStatus.OK);
    }
    @GetMapping("/{id}/messages") @Operation(summary="Read the private coordination chat")
    public List<CoordinationMessageResource> messages(@PathVariable Long id) {
        return queries.handle(new GetContactRequestQuery(iam.currentActor(), id)).stream()
                .map(CareSchedulingResourceAssembler::message).toList();
    }
    @PostMapping("/{id}/messages") @Operation(summary="Send a coordination message; it never grants access to the diary")
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @Valid @RequestBody SendCoordinationMessageResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new SendCoordinationMessageCommand(
                iam.currentActor(), id, r.body())), CareSchedulingResourceAssembler::message, HttpStatus.CREATED);
    }
}
