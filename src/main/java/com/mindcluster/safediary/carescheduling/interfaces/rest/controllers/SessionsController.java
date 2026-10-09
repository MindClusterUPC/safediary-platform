package com.mindcluster.safediary.carescheduling.interfaces.rest.controllers;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.application.queryservices.CareSchedulingQueryService;
import com.mindcluster.safediary.carescheduling.domain.model.commands.*;
import com.mindcluster.safediary.carescheduling.domain.model.queries.GetSessionDetailsQuery;
import com.mindcluster.safediary.carescheduling.interfaces.rest.resources.*;
import com.mindcluster.safediary.carescheduling.interfaces.rest.transform.CareSchedulingResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/appointments/{appointmentId}/session", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Care Scheduling - Sessions")
public class SessionsController {
    private final CareSchedulingCommandService commands;
    private final CareSchedulingQueryService queries;
    private final IamClient iam;

    @GetMapping @Operation(summary="Participant: session status of a confirmed appointment")
    public ResponseEntity<ClinicalSessionResource> get(@PathVariable Long appointmentId) {
        return queries.handle(new GetSessionDetailsQuery(iam.currentActor(), appointmentId))
                .map(session -> CareSchedulingResourceAssembler.session(session, false))
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/access") @Operation(summary="Participant: join the video call inside the access window")
    public ResponseEntity<?> join(@PathVariable Long appointmentId) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new JoinSessionCommand(
                iam.currentActor(), appointmentId)), session -> CareSchedulingResourceAssembler.session(session, true), HttpStatus.OK);
    }
    @PostMapping("/closure") @Operation(summary="Psychologist: close the attention with its operational outcome")
    public ResponseEntity<?> close(@PathVariable Long appointmentId, @Valid @RequestBody CloseSessionResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new CompleteSessionCommand(
                iam.currentActor(), appointmentId, r.outcome())), session -> CareSchedulingResourceAssembler.session(session, false), HttpStatus.OK);
    }
}
