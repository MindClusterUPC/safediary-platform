package com.mindcluster.safediary.profiles.interfaces.rest.controllers;

import com.mindcluster.safediary.profiles.application.commandservices.ProfileCommandService;
import com.mindcluster.safediary.profiles.application.queryservices.ProfileQueryService;
import com.mindcluster.safediary.profiles.domain.model.commands.RegisterPatientCommand;
import com.mindcluster.safediary.profiles.domain.model.commands.UpdateEmergencyContactCommand;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPatientsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPatientByIdQuery;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.PatientResponseResource;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.RegisterPatientResource;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.UpdateEmergencyContactResource;
import com.mindcluster.safediary.profiles.interfaces.rest.transform.PatientResourceAssembler;
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
@RequestMapping(value = "/api/v1/patients", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Patients", description = "Patient Profile & Emergency Contact Management")
public class PatientsController {

    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public PatientsController(ProfileCommandService profileCommandService,
                              ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a new patient profile with emergency contact")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegisterPatientResource resource) {
        var command = new RegisterPatientCommand(
                resource.firstName(),
                resource.lastName(),
                resource.email(),
                resource.password(),
                resource.emergencyContactName(),
                resource.emergencyContactRelationship(),
                resource.emergencyContactPhone(),
                resource.emergencyContactEmail()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                PatientResourceAssembler::toResource,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient profile by ID")
    public ResponseEntity<PatientResponseResource> getPatientById(@PathVariable Long id) {
        return profileQueryService.handle(new GetPatientByIdQuery(id))
                .map(PatientResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all registered patients")
    public ResponseEntity<List<PatientResponseResource>> getAllPatients() {
        var patients = profileQueryService.handle(new GetAllPatientsQuery()).stream()
                .map(PatientResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(patients);
    }

    @PutMapping(value = "/{id}/emergency-contact", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update patient's emergency contact details")
    public ResponseEntity<?> updateEmergencyContact(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateEmergencyContactResource resource) {
        var command = new UpdateEmergencyContactCommand(
                id,
                resource.name(),
                resource.relationship(),
                resource.phoneNumber(),
                resource.email()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                PatientResourceAssembler::toResource,
                HttpStatus.OK
        );
    }
}
