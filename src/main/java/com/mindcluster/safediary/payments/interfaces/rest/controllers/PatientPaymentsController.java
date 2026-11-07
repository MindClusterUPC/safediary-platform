package com.mindcluster.safediary.payments.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.payments.application.queryservices.PatientPaymentsQueryService;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentHistoryItemResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodResource;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments - Patient Wallet & History", description = "Patient transaction history and saved payment methods")
public class PatientPaymentsController {

    private final PatientPaymentsQueryService patientPaymentsQueryService;
    private final IamContextFacade iamContextFacade;

    public PatientPaymentsController(PatientPaymentsQueryService patientPaymentsQueryService,
                                     @Autowired(required = false) IamContextFacade iamContextFacade) {
        this.patientPaymentsQueryService = patientPaymentsQueryService;
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

    @GetMapping("/history")
    @Operation(summary = "Get transaction history for a patient", description = "Returns historical list of payments and charges made by the patient (consultations, subscriptions).")
    public ResponseEntity<?> getTransactionHistory(@RequestParam Long patientAccountId) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        if (patientAccountId == null || patientAccountId <= 0) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("patientAccountId", "patientAccountId is required and must be positive")
            );
        }

        List<PaymentHistoryItemResource> history = patientPaymentsQueryService.getTransactionHistory(patientAccountId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/methods")
    @Operation(summary = "Get saved payment methods for a patient", description = "Returns saved cards and payment methods in Stripe / digital wallet for the patient.")
    public ResponseEntity<?> getPaymentMethods(@RequestParam Long patientAccountId) {
        var roleCheck = checkPatientRole();
        if (roleCheck != null) return roleCheck;

        if (patientAccountId == null || patientAccountId <= 0) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("patientAccountId", "patientAccountId is required and must be positive")
            );
        }

        List<PaymentMethodResource> methods = patientPaymentsQueryService.getSavedPaymentMethods(patientAccountId);
        return ResponseEntity.ok(methods);
    }
}
