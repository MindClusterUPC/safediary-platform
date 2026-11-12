package com.mindcluster.safediary.rutines.interfaces.rest.controllers;

import com.mindcluster.safediary.rutines.application.commandservices.RoutineCommandService;
import com.mindcluster.safediary.rutines.application.queryservices.RoutineQueryService;
import com.mindcluster.safediary.rutines.domain.model.commands.CreateDailyRoutineCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.ToggleDailyRoutineActiveCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.ToggleRoutineNotificationCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.UpdateDailyRoutineCommand;
import com.mindcluster.safediary.rutines.domain.model.queries.GetAllRoutinesByUserIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetDailyRoutineByIdQuery;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.CreateDailyRoutineResource;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.DailyRoutineResponseResource;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.ToggleRoutineNotificationResource;
import com.mindcluster.safediary.rutines.interfaces.rest.resources.UpdateDailyRoutineResource;
import com.mindcluster.safediary.rutines.interfaces.rest.transform.DailyRoutineResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.mindcluster.safediary.rutines.interfaces.rest.RutinesAccountGuard;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineDetails;
import com.mindcluster.safediary.rutines.domain.model.commands.DeleteDailyRoutineCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.CompleteDailyRoutineCommand;

@RestController
@RequestMapping(value = "/api/v1/daily-routines", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Daily Routines", description = "Daily Routine Management")
public class DailyRoutinesController {

    private final RoutineCommandService routineCommandService;
    private final RoutineQueryService routineQueryService;
    private final RutinesAccountGuard accountGuard;

    public DailyRoutinesController(RoutineCommandService routineCommandService,
                                   RoutineQueryService routineQueryService, RutinesAccountGuard accountGuard) {
        this.routineCommandService = routineCommandService;
        this.routineQueryService = routineQueryService;
        this.accountGuard = accountGuard;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new daily routine for a patient")
    public ResponseEntity<?> createDailyRoutine(@Valid @RequestBody CreateDailyRoutineResource resource) {
        accountGuard.requireOwnPatient(resource.patientId());
        var command = new CreateDailyRoutineCommand(
                resource.patientId(),
                resource.title(),
                resource.frequencyDays(),
                resource.isNotificationActive(),
                new RoutineDetails(resource.description(), resource.time(), resource.minutes(), resource.kind())
        );
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DailyRoutineResourceAssembler::toResource,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get daily routine by ID")
    public ResponseEntity<DailyRoutineResponseResource> getDailyRoutineById(@PathVariable Long id) {
        accountGuard.requireOwnRoutine(id);
        return routineQueryService.handle(new GetDailyRoutineByIdQuery(id))
                .map(DailyRoutineResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "List all routines for a specific patient")
    public ResponseEntity<List<DailyRoutineResponseResource>> getAllRoutinesByPatientId(@PathVariable Long patientId) {
        accountGuard.requireOwnPatient(patientId);
        var routines = routineQueryService.handle(new GetAllRoutinesByUserIdQuery(patientId)).stream()
                .map(DailyRoutineResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(routines);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an existing daily routine")
    public ResponseEntity<?> updateDailyRoutine(@PathVariable Long id,
                                                @Valid @RequestBody UpdateDailyRoutineResource resource) {
        accountGuard.requireOwnRoutine(id);
        var previous = routineQueryService.handle(new GetDailyRoutineByIdQuery(id)).orElseThrow().getDetails();
        var command = new UpdateDailyRoutineCommand(
                id,
                resource.title(),
                resource.isNotificationActive(), resource.frequencyDays(),
                new RoutineDetails(resource.description() == null ? previous.description() : resource.description(),
                        resource.time() == null ? previous.time() : resource.time(),
                        resource.minutes() == null ? previous.minutes() : resource.minutes(),
                        resource.kind() == null ? previous.kind() : resource.kind())
        );
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DailyRoutineResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @PatchMapping(value = "/{id}/notification", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Toggle notification status for a daily routine")
    public ResponseEntity<?> toggleRoutineNotification(@PathVariable Long id,
                                                       @Valid @RequestBody ToggleRoutineNotificationResource resource) {
        accountGuard.requireOwnRoutine(id);
        var command = new ToggleRoutineNotificationCommand(
                id,
                resource.isEnabled()
        );
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DailyRoutineResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle active status for a daily routine")
    public ResponseEntity<?> toggleRoutineActive(@PathVariable Long id) {
        accountGuard.requireOwnRoutine(id);
        var command = new ToggleDailyRoutineActiveCommand(id);
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DailyRoutineResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoutine(@PathVariable Long id) {
        accountGuard.requireOwnRoutine(id);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                routineCommandService.handle(new DeleteDailyRoutineCommand(id)), ignored -> null, HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/completion")
    public ResponseEntity<?> completeRoutine(@PathVariable Long id) {
        accountGuard.requireOwnRoutine(id);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                routineCommandService.handle(new CompleteDailyRoutineCommand(id)),
                DailyRoutineResourceAssembler::toResource, HttpStatus.OK);
    }
}
