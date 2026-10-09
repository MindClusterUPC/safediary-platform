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
@RequestMapping(value="/api/v1/reviews", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Clinician Directory - Review Actions")
public class ReviewActionsController {
    private final DirectoryCommandService commands;
    private final DirectoryQueryService queries;
    private final IamRoleClient iam;
    @PutMapping("/{id}/helpful-vote") @Operation(summary="Idempotently mark or unmark a review as helpful")
    public ResponseEntity<?> vote(@PathVariable Long id, @Valid @RequestBody SetHelpfulVoteResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new ToggleReviewHelpfulVoteCommand(iam.currentActor(), id, r.helpful())), vote -> new ReviewHelpfulVoteResource(vote.reviewId(), vote.active(), queries.helpfulCount(vote.reviewId())), HttpStatus.OK);
    }
    @PostMapping("/{id}/reports") @Operation(summary="Submit a private moderation report without hiding the review")
    public ResponseEntity<?> report(@PathVariable Long id, @Valid @RequestBody ReportReviewResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new ReportReviewCommand(iam.currentActor(), id, r.reason(), r.comment())), DirectoryResourceAssembler::report, HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}") @Operation(summary="Delete your own review with explicit confirmation")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam(defaultValue="false") boolean confirmed) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new DeleteOwnReviewCommand(iam.currentActor(), id, confirmed)), r -> new DeletedReviewResource(r.getId(), r.getStatus()), HttpStatus.OK);
    }
}
