package com.mindcluster.safediary.carescheduling.interfaces.rest.controllers;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.domain.model.commands.RequestAuthorizedSummaryCommand;
import com.mindcluster.safediary.carescheduling.interfaces.rest.transform.CareSchedulingResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/appointments/{appointmentId}/authorized-summary", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Care Scheduling - Authorized Summary")
public class AuthorizedSummaryController {
    private final CareSchedulingCommandService commands;
    private final IamClient iam;

    @GetMapping @Operation(summary="Read the patient emotional summary after IAM validates an active consent (audited)")
    public ResponseEntity<?> get(@PathVariable Long appointmentId) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new RequestAuthorizedSummaryCommand(
                iam.currentActor(), appointmentId)), CareSchedulingResourceAssembler::summary, HttpStatus.OK);
    }
}
