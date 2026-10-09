package com.mindcluster.safediary.payments.interfaces.rest.controllers;

import com.mindcluster.safediary.payments.application.commandservices.SubscriptionCommandService;
import com.mindcluster.safediary.payments.domain.model.commands.ProcessStripeWebhookCommand;
import com.mindcluster.safediary.shared.interfaces.rest.resources.MessageResource;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/payments/webhooks/stripe")
@Tag(name = "Payments - Webhooks", description = "Stripe Webhook Event Receiver")
public class StripeWebhookController {

    private final SubscriptionCommandService subscriptionCommandService;

    public StripeWebhookController(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Handle Stripe Webhook Events",
            description = "Receives signed Stripe webhook notifications (e.g., checkout.session.completed, customer.subscription.deleted), verifies signature cryptographically, and processes event idempotently."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook verified and processed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid Stripe signature"),
            @ApiResponse(responseCode = "400", description = "Malformed webhook payload")
    })
    public ResponseEntity<?> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader
    ) {
        var command = new ProcessStripeWebhookCommand(payload, signatureHeader);
        var result = subscriptionCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ignored -> new MessageResource("Webhook processed successfully"),
                HttpStatus.OK
        );
    }
}
