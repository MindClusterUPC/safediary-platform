package com.mindcluster.safediary.payments.interfaces.rest.controllers;

import com.mindcluster.safediary.payments.application.commandservices.SubscriptionCommandService;
import com.mindcluster.safediary.payments.application.queryservices.SubscriptionQueryService;
import com.mindcluster.safediary.payments.domain.model.commands.CancelSubscriptionCommand;
import com.mindcluster.safediary.payments.domain.model.commands.CreateSubscriptionCheckoutCommand;
import com.mindcluster.safediary.payments.domain.model.queries.GetSubscriptionByPatientIdQuery;
import com.mindcluster.safediary.payments.interfaces.rest.resources.CancelSubscriptionResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.CheckoutResponseResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.CreateCheckoutResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.SubscriptionResponseResource;
import com.mindcluster.safediary.payments.interfaces.rest.transform.SubscriptionResourceAssembler;
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
@RequestMapping(value = "/api/v1/payments/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments - Subscriptions", description = "Patient Plan Subscriptions and Stripe Checkout")
public class SubscriptionsController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;

    public SubscriptionsController(SubscriptionCommandService subscriptionCommandService,
                                   SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @PostMapping(value = "/checkout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create subscription checkout session or activate free plan",
            description = "Initiates a Stripe Checkout session for paid plans (TERRA or ASTRUM), or directly activates the FREE plan without payment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checkout session created or free plan activated",
                    content = @Content(schema = @Schema(implementation = CheckoutResponseResource.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Internal error connecting with Stripe")
    })
    public ResponseEntity<?> createCheckout(
            @Valid @RequestBody CreateCheckoutResource resource,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader
    ) {
        var command = new CreateSubscriptionCheckoutCommand(
                resource.patientAccountId(),
                resource.plan(),
                resource.successUrl(),
                resource.cancelUrl(),
                idempotencyKeyHeader
        );

        var result = subscriptionCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SubscriptionResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get current subscription details for a patient",
            description = "Retrieves active tier and validity period. If no subscription exists yet, returns a default FREE plan."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription details found",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponseResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid patient ID")
    })
    public ResponseEntity<?> getCurrentSubscription(
            @RequestParam(value = "patientAccountId", required = false) Long patientAccountIdParam,
            @RequestHeader(value = "X-Patient-Account-Id", required = false) Long patientAccountHeader
    ) {
        Long effectiveId = patientAccountIdParam != null ? patientAccountIdParam : patientAccountHeader;
        if (effectiveId == null || effectiveId <= 0) {
            return ResponseEntity.badRequest().body("patientAccountId must be provided via query param or X-Patient-Account-Id header");
        }

        var query = new GetSubscriptionByPatientIdQuery(effectiveId);
        var result = subscriptionQueryService.handle(query);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SubscriptionResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @PostMapping(value = "/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Cancel recurring subscription",
            description = "Cancels auto-renewal on Stripe and marks the subscription as CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponseResource.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<?> cancelSubscription(@Valid @RequestBody CancelSubscriptionResource resource) {
        var command = new CancelSubscriptionCommand(resource.patientAccountId());
        var result = subscriptionCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SubscriptionResourceAssembler::toResource,
                HttpStatus.OK
        );
    }
}
