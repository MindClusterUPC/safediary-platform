package com.mindcluster.safediary.payments.interfaces.rest.controllers;

import com.mindcluster.safediary.payments.application.commandservices.PatientPaymentMethodCommandService;
import com.mindcluster.safediary.payments.application.queryservices.PatientPaymentsQueryService;
import com.mindcluster.safediary.payments.domain.model.commands.CreatePaymentMethodSetupCommand;
import com.mindcluster.safediary.payments.domain.model.commands.SetDefaultPaymentMethodCommand;
import com.mindcluster.safediary.payments.interfaces.rest.PaymentsAccountGuard;
import com.mindcluster.safediary.payments.interfaces.rest.resources.CreatePaymentMethodSetupResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentHistoryItemResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodSetupResponseResource;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments - Patient Wallet & History", description = "Patient transaction history and saved payment methods")
public class PatientPaymentsController {

    private final PatientPaymentsQueryService patientPaymentsQueryService;
    private final PatientPaymentMethodCommandService patientPaymentMethodCommandService;
    private final PaymentsAccountGuard accountGuard;

    public PatientPaymentsController(PatientPaymentsQueryService patientPaymentsQueryService,
                                     PaymentsAccountGuard accountGuard,
                                     @Autowired(required = false) PatientPaymentMethodCommandService patientPaymentMethodCommandService) {
        this.accountGuard = accountGuard;
        this.patientPaymentsQueryService = patientPaymentsQueryService;
        this.patientPaymentMethodCommandService = patientPaymentMethodCommandService;
    }

    @GetMapping("/history")
    @Operation(summary = "Get transaction history for a patient", description = "Returns historical list of payments and charges made by the patient (consultations, subscriptions).")
    public ResponseEntity<?> getTransactionHistory(@RequestParam(name = "patientAccountId", required = false) Long requestedAccountId) {
        Long patientAccountId = accountGuard.requireOwnAccountWithRole(requestedAccountId, "PATIENT");

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
    public ResponseEntity<?> getPaymentMethods(@RequestParam(name = "patientAccountId", required = false) Long requestedAccountId) {
        Long patientAccountId = accountGuard.requireOwnAccountWithRole(requestedAccountId, "PATIENT");

        if (patientAccountId == null || patientAccountId <= 0) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("patientAccountId", "patientAccountId is required and must be positive")
            );
        }

        List<PaymentMethodResource> methods = patientPaymentsQueryService.getSavedPaymentMethods(patientAccountId);
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/methods/setup")
    @Operation(
            summary = "Create Stripe Checkout setup session to save a card or digital wallet",
            description = "Creates or reuses the patient's Stripe Customer and returns a Stripe Checkout URL in setup mode so a payment method can be saved without charging."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment method setup session created",
                    content = @Content(schema = @Schema(implementation = PaymentMethodSetupResponseResource.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Error communicating with Stripe")
    })
    public ResponseEntity<?> createPaymentMethodSetupSession(
            @RequestBody(required = false) CreatePaymentMethodSetupResource resource
    ) {
        Long requestedAccountId = resource != null ? resource.patientAccountId() : null;
        Long patientAccountId = accountGuard.requireOwnAccountWithRole(requestedAccountId, "PATIENT");

        if (patientAccountId == null || patientAccountId <= 0) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("patientAccountId", "patientAccountId is required and must be positive")
            );
        }

        var command = new CreatePaymentMethodSetupCommand(
                patientAccountId,
                resource != null ? resource.successUrl() : null,
                resource != null ? resource.cancelUrl() : null
        );
        var result = patientPaymentMethodCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                dto -> new PaymentMethodSetupResponseResource(
                        dto.patientAccountId(),
                        dto.checkoutUrl(),
                        dto.sessionId(),
                        dto.message()
                ),
                HttpStatus.OK
        );
    }

    @PutMapping("/methods/{paymentMethodId}/default")
    @Operation(
            summary = "Set default saved payment method",
            description = "Updates the patient's Stripe Customer default payment method and returns the updated list."
    )
    public ResponseEntity<?> setDefaultPaymentMethod(
            @PathVariable String paymentMethodId,
            @RequestParam(name = "patientAccountId", required = false) Long requestedAccountId
    ) {
        Long patientAccountId = accountGuard.requireOwnAccountWithRole(requestedAccountId, "PATIENT");

        if (patientAccountId == null || patientAccountId <= 0) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("patientAccountId", "patientAccountId is required and must be positive")
            );
        }

        var command = new SetDefaultPaymentMethodCommand(patientAccountId, paymentMethodId);
        var result = patientPaymentMethodCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, methods -> methods, HttpStatus.OK);
    }
}
