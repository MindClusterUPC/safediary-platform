package com.mindcluster.safediary.payments.interfaces.rest.controllers;

import com.mindcluster.safediary.payments.application.commandservices.ConsultationPaymentCommandService;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConsultationCheckoutCommand;
import com.mindcluster.safediary.payments.interfaces.rest.PaymentsAccountGuard;
import com.mindcluster.safediary.payments.interfaces.rest.resources.ConsultationCheckoutResponseResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.CreateConsultationCheckoutResource;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/payments/consultations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments - Consultations", description = "Marketplace consultation payments and split transfers to psychologists")
public class ConsultationsPaymentController {

    private final ConsultationPaymentCommandService consultationPaymentCommandService;
    private final PaymentsAccountGuard accountGuard;

    public ConsultationsPaymentController(ConsultationPaymentCommandService consultationPaymentCommandService,
                                          PaymentsAccountGuard accountGuard) {
        this.consultationPaymentCommandService = consultationPaymentCommandService;
        this.accountGuard = accountGuard;
    }

    @PostMapping(value = "/checkout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create consultation checkout session with split payment / destination charge",
            description = "Initiates a Stripe Checkout session in PAYMENT mode with application fee commission and transfer destination to the psychologist's connected account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultation checkout session created",
                    content = @Content(schema = @Schema(implementation = ConsultationCheckoutResponseResource.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "422", description = "Psychologist has not completed Stripe Connect onboarding"),
            @ApiResponse(responseCode = "500", description = "Internal error connecting with Stripe")
    })
    public ResponseEntity<?> createConsultationCheckout(@Valid @RequestBody CreateConsultationCheckoutResource resource) {
        Long patientAccountId = accountGuard.requireOwnAccountWithRole(resource.patientAccountId(), "PATIENT");
        var command = new CreateConsultationCheckoutCommand(
                patientAccountId,
                resource.appointmentId(),
                resource.successUrl(),
                resource.cancelUrl()
        );

        var result = consultationPaymentCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                dto -> new ConsultationCheckoutResponseResource(
                        dto.appointmentId(),
                        dto.patientAccountId(),
                        dto.psychologistAccountId(),
                        dto.amount(),
                        dto.currency(),
                        dto.checkoutUrl(),
                        dto.sessionId()
                ),
                HttpStatus.OK
        );
    }
}
