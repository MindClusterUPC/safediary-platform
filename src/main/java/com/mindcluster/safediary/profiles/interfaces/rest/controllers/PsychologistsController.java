package com.mindcluster.safediary.profiles.interfaces.rest.controllers;

import com.mindcluster.safediary.profiles.application.commandservices.ProfileCommandService;
import com.mindcluster.safediary.profiles.application.queryservices.ProfileQueryService;
import com.mindcluster.safediary.profiles.domain.model.commands.ConfigurePsychologistAvailabilityCommand;
import com.mindcluster.safediary.profiles.domain.model.commands.RegisterPsychologistCommand;
import com.mindcluster.safediary.profiles.domain.model.commands.UpdatePsychologistProfileCommand;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPsychologistsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPsychologistByIdQuery;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.ConfigureAvailabilityResource;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.PsychologistResponseResource;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.RegisterPsychologistResource;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.UpdatePsychologistProfileResource;
import com.mindcluster.safediary.profiles.interfaces.rest.transform.PsychologistResourceAssembler;
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
@RequestMapping(value = "/api/v1/psychologists", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Psychologists", description = "Psychologist Profile, Availability & Modality Management")
public class PsychologistsController {

    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public PsychologistsController(ProfileCommandService profileCommandService,
                                   ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a new psychologist profile")
    public ResponseEntity<?> registerPsychologist(@Valid @RequestBody RegisterPsychologistResource resource) {
        var command = new RegisterPsychologistCommand(
                resource.firstName(),
                resource.lastName(),
                resource.email(),
                resource.costPerSession(),
                resource.durationMinutes(),
                resource.currency(),
                resource.videoCallEnabled(),
                resource.encryptedChatEnabled(),
                resource.bio(),
                resource.categories(),
                resource.certificateTitles()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                PsychologistResourceAssembler::toResource,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get psychologist profile by ID")
    public ResponseEntity<PsychologistResponseResource> getPsychologistById(@PathVariable Long id) {
        return profileQueryService.handle(new GetPsychologistByIdQuery(id))
                .map(PsychologistResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all registered psychologists")
    public ResponseEntity<List<PsychologistResponseResource>> getAllPsychologists() {
        var list = profileQueryService.handle(new GetAllPsychologistsQuery()).stream()
                .map(PsychologistResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping(value = "/{id}/availability", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Configure psychologist weekly recurring availability (returns error if slots overlap)")
    public ResponseEntity<?> configureAvailability(@PathVariable Long id,
                                                   @Valid @RequestBody ConfigureAvailabilityResource resource) {
        var slotEntries = resource.slots().stream()
                .map(s -> new ConfigurePsychologistAvailabilityCommand.AvailabilitySlotEntry(
                        s.dayOfWeek(), s.startTime(), s.endTime()
                ))
                .toList();

        var command = new ConfigurePsychologistAvailabilityCommand(id, slotEntries);
        var result = profileCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                PsychologistResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update psychologist pricing, modalities, categories and bio")
    public ResponseEntity<?> updatePsychologistProfile(@PathVariable Long id,
                                                      @Valid @RequestBody UpdatePsychologistProfileResource resource) {
        var command = new UpdatePsychologistProfileCommand(
                id,
                resource.costPerSession(),
                resource.durationMinutes(),
                resource.currency(),
                resource.videoCallEnabled(),
                resource.encryptedChatEnabled(),
                resource.bio(),
                resource.categories(),
                resource.certificateTitles()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                PsychologistResourceAssembler::toResource,
                HttpStatus.OK
        );
    }
}
