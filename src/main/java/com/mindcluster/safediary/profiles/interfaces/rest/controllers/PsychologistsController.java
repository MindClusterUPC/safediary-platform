package com.mindcluster.safediary.profiles.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/psychologists", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Psychologists", description = "Psychologist Profile, Availability & Modality Management")
public class PsychologistsController {

    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;
    private final IamContextFacade iamContextFacade;

    public PsychologistsController(ProfileCommandService profileCommandService,
                                   ProfileQueryService profileQueryService,
                                   @Autowired(required = false) IamContextFacade iamContextFacade) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
        this.iamContextFacade = iamContextFacade;
    }

    private Optional<IamAccountDto> requirePsychologistRole() {
        if (iamContextFacade == null) {
            return Optional.empty();
        }
        var account = iamContextFacade.fetchAuthenticatedAccount()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to manage psychologist profiles"));
        if (!"PSYCHOLOGIST".equalsIgnoreCase(account.role())) {
            throw new AccessDeniedException("Operation restricted to PSYCHOLOGIST role");
        }
        return Optional.of(account);
    }

    private void requireOwnPsychologist(Long psychologistId) {
        var accountOpt = requirePsychologistRole();
        if (accountOpt.isEmpty()) {
            return;
        }
        var account = accountOpt.get();
        profileQueryService.handle(new GetPsychologistByIdQuery(psychologistId))
                .filter(psychologist -> !psychologist.getEmail().value().equalsIgnoreCase(account.email()))
                .ifPresent(psychologist -> {
                    throw new AccessDeniedException("The psychologist profile does not belong to the signed-in user");
                });
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a new psychologist profile")
    public ResponseEntity<?> registerPsychologist(@Valid @RequestBody RegisterPsychologistResource resource) {
        requirePsychologistRole().ifPresent(account -> {
            if (!account.email().equalsIgnoreCase(resource.email().trim())) {
                throw new AccessDeniedException("Psychologist profile email must match the signed-in account");
            }
        });
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
        requireOwnPsychologist(id);
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
        requireOwnPsychologist(id);
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
