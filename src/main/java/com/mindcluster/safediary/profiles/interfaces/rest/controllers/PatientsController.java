package com.mindcluster.safediary.profiles.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.payments.interfaces.acl.PaymentsContextFacade;
import com.mindcluster.safediary.profiles.application.commandservices.ProfileCommandService;
import com.mindcluster.safediary.profiles.application.queryservices.ProfileQueryService;
import com.mindcluster.safediary.profiles.domain.model.commands.*;
import com.mindcluster.safediary.profiles.domain.model.queries.GetAllPatientsQuery;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPatientByIdQuery;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.*;
import com.mindcluster.safediary.profiles.interfaces.rest.transform.PatientResourceAssembler;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final PaymentsContextFacade paymentsContextFacade;
    private final IamContextFacade iamContextFacade;

    public PatientsController(ProfileCommandService profileCommandService,
                              ProfileQueryService profileQueryService,
                              PaymentsContextFacade paymentsContextFacade,
                              @Autowired(required = false) IamContextFacade iamContextFacade) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
        this.paymentsContextFacade = paymentsContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    private ResponseEntity<?> checkPatientRole() {
        if (iamContextFacade != null) {
            var authAccount = iamContextFacade.fetchAuthenticatedAccount();
            if (authAccount.isPresent() && !"PATIENT".equalsIgnoreCase(authAccount.get().role())) {
                return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.forbidden("Operation restricted to PATIENT role")
                );
            }
        }
        return null;
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
                patient -> {
                    var planStatus = paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
                    return PatientResourceAssembler.toResource(patient, planStatus);
                },
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient profile by ID")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        return profileQueryService.handle(new GetPatientByIdQuery(id))
                .map(patient -> {
                    var planStatus = paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
                    return PatientResourceAssembler.toResource(patient, planStatus);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all registered patients")
    public ResponseEntity<?> getAllPatients() {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        var patients = profileQueryService.handle(new GetAllPatientsQuery()).stream()
                .map(patient -> {
                    var planStatus = paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
                    return PatientResourceAssembler.toResource(patient, planStatus);
                })
                .toList();
        return ResponseEntity.ok(patients);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update patient personal information (first and last name)")
    public ResponseEntity<?> updatePatientProfile(@PathVariable Long id,
                                                 @Valid @RequestBody UpdatePatientProfileResource resource) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        var command = new UpdatePatientProfileCommand(
                id,
                resource.firstName(),
                resource.lastName()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                patient -> {
                    var planStatus = paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
                    return PatientResourceAssembler.toResource(patient, planStatus);
                },
                HttpStatus.OK
        );
    }

    @DeleteMapping(value = "/{id}/emergency-contact")
    @Operation(summary = "Remove patient's emergency contact")
    public ResponseEntity<?> removeEmergencyContact(@PathVariable Long id) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        var command = new RemoveEmergencyContactCommand(id);
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                patient -> {
                    var planStatus = paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
                    return PatientResourceAssembler.toResource(patient, planStatus);
                },
                HttpStatus.OK
        );
    }

    @PutMapping(value = "/{id}/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update patient email validating current password against IAM")
    public ResponseEntity<?> updatePatientEmail(@PathVariable Long id,
                                               @Valid @RequestBody UpdatePatientEmailResource resource) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        var command = new UpdatePatientEmailCommand(
                id,
                resource.newEmail(),
                resource.currentPassword()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                patient -> {
                    var planStatus = paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
                    return PatientResourceAssembler.toResource(patient, planStatus);
                },
                HttpStatus.OK
        );
    }

    @PutMapping(value = "/{id}/emergency-contact", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update patient's emergency contact details")
    public ResponseEntity<?> updateEmergencyContact(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateEmergencyContactResource resource) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

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
                patient -> {
                    var planStatus = paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
                    return PatientResourceAssembler.toResource(patient, planStatus);
                },
                HttpStatus.OK
        );
    }
}
