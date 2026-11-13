package com.mindcluster.safediary.profiles.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import com.mindcluster.safediary.payments.interfaces.acl.PaymentsContextFacade;
import com.mindcluster.safediary.payments.interfaces.acl.dto.PatientPlanStatusDto;
import com.mindcluster.safediary.profiles.application.commandservices.ProfileCommandService;
import com.mindcluster.safediary.profiles.application.queryservices.ProfileQueryService;
import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.commands.*;
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
import com.mindcluster.safediary.profiles.domain.model.queries.GetPatientByEmailQuery;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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

    /** Rejects a patient profile that does not belong to the signed-in account (matched by e-mail). */
    private void requireOwnPatient(Long patientId) {
        var account = iamContextFacade.fetchAuthenticatedAccount()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to access a patient profile"));
        profileQueryService.handle(new GetPatientByIdQuery(patientId))
                .filter(patient -> !patient.getEmail().value().equalsIgnoreCase(account.email()))
                .ifPresent(patient -> {
                    throw new AccessDeniedException("The patient profile does not belong to the signed-in user");
                });
    }

    /**
     * Resolves subscription status using the IAM account ID (which keys Payments subscriptions)
     * rather than the profiles.patients table primary key, avoiding mismatches when sequences diverge.
     */
    private PatientPlanStatusDto resolvePlanStatus(Patient patient) {
        if (iamContextFacade == null) {
            return paymentsContextFacade.fetchPatientPlanStatus(patient.getId());
        }
        var accountIdOpt = iamContextFacade.fetchAuthenticatedAccount()
                .filter(acc -> acc.email().equalsIgnoreCase(patient.getEmail().value()))
                .map(IamAccountDto::accountId)
                .or(() -> iamContextFacade.fetchAccountByEmail(patient.getEmail().value()).map(IamAccountDto::accountId));
        return accountIdOpt
                .map(paymentsContextFacade::fetchPatientPlanStatus)
                .orElseGet(() -> new PatientPlanStatusDto(null, "FREE", "ACTIVE", null, true));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the patient profile of the signed-in account")
    public ResponseEntity<?> getCurrentPatient() {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        var account = iamContextFacade.fetchAuthenticatedAccount()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to access your profile"));
        return profileQueryService.handle(new GetPatientByEmailQuery(account.email()))
                .map(patient -> PatientResourceAssembler.toResource(patient, resolvePlanStatus(patient)))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a new patient profile with emergency contact")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegisterPatientResource resource) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;
        if (iamContextFacade != null) {
            var account = iamContextFacade.fetchAuthenticatedAccount()
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to register your patient profile"));
            if (!account.email().equalsIgnoreCase(resource.email().trim())) {
                throw new AccessDeniedException("Patient profile email must match the signed-in account");
            }
        }

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
                patient -> PatientResourceAssembler.toResource(patient, resolvePlanStatus(patient)),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient profile by ID")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;
        requireOwnPatient(id);

        return profileQueryService.handle(new GetPatientByIdQuery(id))
                .map(patient -> PatientResourceAssembler.toResource(patient, resolvePlanStatus(patient)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Not allowed: patient profiles are private; use /api/v1/patients/me")
    public ResponseEntity<?> getAllPatients() {
        // Patient profiles are private health data, so no account may list every patient.
        throw new AccessDeniedException("Listing every patient is not allowed; use /api/v1/patients/me");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update patient personal information (first and last name)")
    public ResponseEntity<?> updatePatientProfile(@PathVariable Long id,
                                                 @Valid @RequestBody UpdatePatientProfileResource resource) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;
        requireOwnPatient(id);

        var command = new UpdatePatientProfileCommand(
                id,
                resource.firstName(),
                resource.lastName()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                patient -> PatientResourceAssembler.toResource(patient, resolvePlanStatus(patient)),
                HttpStatus.OK
        );
    }

    @DeleteMapping(value = "/{id}/emergency-contact")
    @Operation(summary = "Remove patient's emergency contact")
    public ResponseEntity<?> removeEmergencyContact(@PathVariable Long id) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;
        requireOwnPatient(id);

        var command = new RemoveEmergencyContactCommand(id);
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                patient -> PatientResourceAssembler.toResource(patient, resolvePlanStatus(patient)),
                HttpStatus.OK
        );
    }

    @PutMapping(value = "/{id}/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update patient email validating current password against IAM")
    public ResponseEntity<?> updatePatientEmail(@PathVariable Long id,
                                               @Valid @RequestBody UpdatePatientEmailResource resource) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;
        requireOwnPatient(id);

        var command = new UpdatePatientEmailCommand(
                id,
                resource.newEmail(),
                resource.currentPassword()
        );
        var result = profileCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                patient -> PatientResourceAssembler.toResource(patient, resolvePlanStatus(patient)),
                HttpStatus.OK
        );
    }

    @PutMapping(value = "/{id}/emergency-contact", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update patient's emergency contact details")
    public ResponseEntity<?> updateEmergencyContact(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateEmergencyContactResource resource) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;
        requireOwnPatient(id);

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
                patient -> PatientResourceAssembler.toResource(patient, resolvePlanStatus(patient)),
                HttpStatus.OK
        );
    }
}
