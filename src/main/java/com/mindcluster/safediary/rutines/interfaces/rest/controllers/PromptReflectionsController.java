package com.mindcluster.safediary.rutines.interfaces.rest.controllers;

import com.mindcluster.safediary.rutines.application.commandservices.RoutineCommandService;
import com.mindcluster.safediary.rutines.application.queryservices.RoutineQueryService;
import com.mindcluster.safediary.rutines.domain.model.commands.SubmitDailyReflectionCommand;
import com.mindcluster.safediary.rutines.domain.model.queries.GetPendingDailyReflectionQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetPromptReflectionByIdQuery;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.PromptReflectionResponseResource;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.SubmitDailyReflectionResource;
import com.mindcluster.safediary.rutines.interfaces.rest.transform.PromptReflectionResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/prompt-reflections", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Prompt Reflections", description = "Daily Prompt Reflection Management")
public class PromptReflectionsController {

    private final RoutineCommandService routineCommandService;
    private final RoutineQueryService routineQueryService;

    public PromptReflectionsController(RoutineCommandService routineCommandService,
                                       RoutineQueryService routineQueryService) {
        this.routineCommandService = routineCommandService;
        this.routineQueryService = routineQueryService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a prompt reflection by ID")
    public ResponseEntity<PromptReflectionResponseResource> getReflectionById(@PathVariable Long id) {
        return routineQueryService.handle(new GetPromptReflectionByIdQuery(id))
                .map(PromptReflectionResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}/pending")
    @Operation(summary = "Get the pending daily reflection for a patient")
    public ResponseEntity<PromptReflectionResponseResource> getPendingReflection(@PathVariable Long patientId) {
        return routineQueryService.handle(new GetPendingDailyReflectionQuery(patientId))
                .map(PromptReflectionResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping(value = "/{id}/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit an answer for a pending prompt reflection")
    public ResponseEntity<?> submitReflection(@PathVariable Long id,
                                              @Valid @RequestBody SubmitDailyReflectionResource resource) {
        var command = new SubmitDailyReflectionCommand(
                id,
                resource.answer()
        );
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                PromptReflectionResourceAssembler::toResource,
                HttpStatus.OK
        );
    }
}