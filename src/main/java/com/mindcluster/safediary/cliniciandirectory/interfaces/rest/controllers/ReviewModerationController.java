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
@RequestMapping(value="/api/v1/review-reports", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Clinician Directory - Moderation")
public class ReviewModerationController {
    private final DirectoryCommandService commands;
    private final DirectoryQueryService queries;
    private final IamRoleClient iam;
    @GetMapping @Operation(summary="List moderation reports as moderator or administrator")
    public ResponseEntity<?> reports(@RequestParam(defaultValue="PENDING") ReportStatus status) {
        return ResponseEntity.ok(queries.handle(new GetReviewReportsQuery(iam.currentActor(), status)).stream()
                .map(DirectoryResourceAssembler::report).toList());
    }
    @PatchMapping("/{id}/resolution") @Operation(summary="Dismiss a report or remove the reported review with an audited decision")
    public ResponseEntity<?> resolve(@PathVariable Long id, @Valid @RequestBody ResolveReviewReportResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new ResolveReviewReportCommand(iam.currentActor(), id, r.removeReview(), r.resolution())), DirectoryResourceAssembler::report, HttpStatus.OK);
    }
}
