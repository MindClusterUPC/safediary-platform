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
@RequestMapping(value="/api/v1/clinicians", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Clinician Directory - Profiles")
public class ProfessionalProfileController {
    private final DirectoryCommandService commands;
    private final DirectoryQueryService queries;
    private final IamRoleClient iam;
    @PostMapping @Operation(summary="Create your professional draft profile")
    public ResponseEntity<?> create(@Valid @RequestBody UpsertProfessionalProfileResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new CreateClinicianProfileCommand(iam.currentActor(), r.displayName(), r.professionalTitle(), r.bio(), r.bannerRef(), r.specialties(), r.amount(), r.currency(), r.durationMinutes())), DirectoryResourceAssembler::profile, HttpStatus.CREATED);
    }
    @GetMapping("/me") @Operation(summary="Read your own profile, including drafts and hidden profiles")
    public ResponseEntity<ProfessionalProfileResource> own() {
        return queries.handle(new GetOwnClinicianProfileQuery(iam.currentActor())).map(DirectoryResourceAssembler::profile)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}") @Operation(summary="Update your profile, banner and versioned consultation rate")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UpsertProfessionalProfileResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new UpdateBannerAndRatesCommand(iam.currentActor(), id, r.displayName(), r.professionalTitle(), r.bio(), r.bannerRef(), r.specialties(), r.amount(), r.currency(), r.durationMinutes())), DirectoryResourceAssembler::profile, HttpStatus.OK);
    }
    @PostMapping("/{id}/publication") @Operation(summary="Publish your approved profile")
    public ResponseEntity<?> publish(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new PublishProfileCommand(iam.currentActor(), id)), DirectoryResourceAssembler::profile, HttpStatus.OK);
    }
    @DeleteMapping("/{id}/publication") @Operation(summary="Hide your profile from public directory results")
    public ResponseEntity<?> hide(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new HideProfileCommand(iam.currentActor(), id)), DirectoryResourceAssembler::profile, HttpStatus.OK);
    }
}
