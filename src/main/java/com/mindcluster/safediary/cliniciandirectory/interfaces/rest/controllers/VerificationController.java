package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.controllers;

import com.mindcluster.safediary.cliniciandirectory.application.commandservices.DirectoryCommandService;
import com.mindcluster.safediary.cliniciandirectory.application.queryservices.DirectoryQueryService;
import com.mindcluster.safediary.cliniciandirectory.application.internal.outboundservices.iam.IamRoleClient;
import com.mindcluster.safediary.cliniciandirectory.domain.model.commands.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.queries.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources.*;
import com.mindcluster.safediary.cliniciandirectory.interfaces.rest.transform.DirectoryResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Clinician Directory - Verification")
public class VerificationController {
    private final DirectoryCommandService commands;
    private final DirectoryQueryService queries;
    private final IamRoleClient iam;
    @PostMapping("/clinicians/{id}/verification-requests") @Operation(summary="Submit private credential references for verification")
    public ResponseEntity<?> request(@PathVariable Long id, @Valid @RequestBody RequestVerificationResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new RequestVerificationCommand(iam.currentActor(), id, new ProfessionalCredential(r.licenseNumber(), r.specialty(), r.documentRef()))), DirectoryResourceAssembler::verification, HttpStatus.CREATED);
    }
    @GetMapping("/clinicians/{id}/verification") @Operation(summary="Read latest verification as profile owner or administrator")
    public ResponseEntity<VerificationRequestResource> status(@PathVariable Long id) {
        return queries.handle(new GetVerificationStatusQuery(iam.currentActor(), id)).map(DirectoryResourceAssembler::verification)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/verification-requests") @Operation(summary="List pending credential requests as administrator")
    public ResponseEntity<?> pending() {
        return ResponseEntity.ok(queries.handle(new GetPendingVerificationsQuery(iam.currentActor())).stream()
                .map(DirectoryResourceAssembler::verification).toList());
    }
    @PatchMapping("/verification-requests/{id}/decision") @Operation(summary="Approve or reject credentials as administrator")
    public ResponseEntity<?> decide(@PathVariable Long id, @Valid @RequestBody ReviewCredentialsResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new ReviewCredentialsCommand(iam.currentActor(), id, r.decision(), r.reason())), DirectoryResourceAssembler::verification, HttpStatus.OK);
    }
}
