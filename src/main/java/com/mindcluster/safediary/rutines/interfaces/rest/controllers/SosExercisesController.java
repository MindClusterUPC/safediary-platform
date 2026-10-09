package com.mindcluster.safediary.rutines.interfaces.rest.controllers;

import com.mindcluster.safediary.rutines.application.commandservices.RoutineCommandService;
import com.mindcluster.safediary.rutines.application.queryservices.RoutineQueryService;
import com.mindcluster.safediary.rutines.domain.model.commands.AdvanceSosStepCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.CompleteSosExerciseCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.StartSosExerciseCommand;
import com.mindcluster.safediary.rutines.domain.model.queries.GetSosExerciseByIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetSosExerciseHistoryByPatientIdQuery;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.SosExerciseResponseResource;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.StartSosExerciseResource;
import com.mindcluster.safediary.rutines.interfaces.rest.transform.SosExerciseResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/sos-exercises", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "SOS Exercises", description = "Emergency SOS Exercise Management")
public class SosExercisesController {

    private final RoutineCommandService routineCommandService;
    private final RoutineQueryService routineQueryService;

    public SosExercisesController(RoutineCommandService routineCommandService,
                                  RoutineQueryService routineQueryService) {
        this.routineCommandService = routineCommandService;
        this.routineQueryService = routineQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start a new SOS exercise for a patient")
    public ResponseEntity<?> startSosExercise(@Valid @RequestBody StartSosExerciseResource resource) {
        var command = new StartSosExerciseCommand(
                resource.patientId(),
                resource.exerciseType(),
                resource.totalSteps()
        );
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SosExerciseResourceAssembler::toResource,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SOS exercise state by ID")
    public ResponseEntity<SosExerciseResponseResource> getSosExerciseById(@PathVariable Long id) {
        return routineQueryService.handle(new GetSosExerciseByIdQuery(id))
                .map(SosExerciseResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get the history of SOS exercises for a specific patient")
    public ResponseEntity<List<SosExerciseResponseResource>> getSosExerciseHistory(@PathVariable Long patientId) {
        var exercises = routineQueryService.handle(new GetSosExerciseHistoryByPatientIdQuery(patientId)).stream()
                .map(SosExerciseResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(exercises);
    }

    @PatchMapping("/{id}/advance")
    @Operation(summary = "Advance the current step of an active SOS exercise")
    public ResponseEntity<?> advanceSosStep(@PathVariable Long id) {
        var command = new AdvanceSosStepCommand(id);
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SosExerciseResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark an SOS exercise as completed")
    public ResponseEntity<?> completeSosExercise(@PathVariable Long id) {
        var command = new CompleteSosExerciseCommand(id);
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SosExerciseResourceAssembler::toResource,
                HttpStatus.OK
        );
    }
}