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
@RequestMapping(value="/api/v1/clinicians/{id}/reviews", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Clinician Directory - Reviews")
public class ReviewsController {
    private final DirectoryCommandService commands;
    private final IamRoleClient iam;
    @PostMapping @Operation(summary="Publish one review for your completed appointment")
    public ResponseEntity<?> publish(@PathVariable Long id, @Valid @RequestBody PublishReviewResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new PublishReviewCommand(iam.currentActor(), id, r.appointmentId(), r.rating(), r.text())), review -> DirectoryResourceAssembler.review(review, 0), HttpStatus.CREATED);
    }
}
