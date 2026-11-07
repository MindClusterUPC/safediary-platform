package com.mindcluster.safediary.payments.interfaces.rest.controllers;

import com.mindcluster.safediary.payments.application.commandservices.ConnectedAccountCommandService;
import com.mindcluster.safediary.payments.application.queryservices.ConnectedAccountQueryService;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConnectedAccountCommand;
import com.mindcluster.safediary.payments.domain.model.queries.GetConnectedAccountQuery;
import com.mindcluster.safediary.payments.interfaces.rest.resources.ConnectedAccountResponseResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.CreateConnectedAccountResource;
import com.mindcluster.safediary.shared.interfaces.rest.resources.MessageResource;
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
@RequestMapping(value = "/api/v1/payments/connect", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments - Stripe Connect", description = "Psychologist onboarding, payout configuration, and account links")
public class PsychologistPayoutsController {

    private final ConnectedAccountCommandService connectedAccountCommandService;
    private final ConnectedAccountQueryService connectedAccountQueryService;

    public PsychologistPayoutsController(ConnectedAccountCommandService connectedAccountCommandService,
                                         ConnectedAccountQueryService connectedAccountQueryService) {
        this.connectedAccountCommandService = connectedAccountCommandService;
        this.connectedAccountQueryService = connectedAccountQueryService;
    }

    @PostMapping(value = "/accounts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create or retrieve Stripe Connect Express onboarding link",
            description = "Creates an Express connected account for a psychologist with card_payments and transfers capabilities, and generates the AccountLink onboarding URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboarding link created successfully",
                    content = @Content(schema = @Schema(implementation = ConnectedAccountResponseResource.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Error communicating with Stripe")
    })
    public ResponseEntity<?> createOrGetOnboardingLink(@Valid @RequestBody CreateConnectedAccountResource resource) {
        var command = new CreateConnectedAccountCommand(
                resource.psychologistAccountId(),
                resource.email(),
                resource.refreshUrl(),
                resource.returnUrl()
        );

        var result = connectedAccountCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                dto -> new ConnectedAccountResponseResource(
                        dto.psychologistAccountId(),
                        dto.stripeAccountId(),
                        dto.onboardingUrl(),
                        dto.chargesEnabled(),
                        dto.payoutsEnabled(),
                        dto.detailsSubmitted(),
                        dto.chargesEnabled() && dto.payoutsEnabled()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/accounts/{psychologistAccountId}")
    @Operation(
            summary = "Get Stripe Connect account status for a psychologist",
            description = "Checks whether the psychologist has completed Stripe Express onboarding, and whether charges and payouts are active."
    )
    public ResponseEntity<?> getConnectedAccountStatus(@PathVariable Long psychologistAccountId) {
        var query = new GetConnectedAccountQuery(psychologistAccountId);
        var result = connectedAccountQueryService.handle(query);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                dto -> new ConnectedAccountResponseResource(
                        dto.psychologistAccountId(),
                        dto.stripeAccountId(),
                        null,
                        dto.chargesEnabled(),
                        dto.payoutsEnabled(),
                        dto.detailsSubmitted(),
                        dto.ready()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/refresh")
    @Operation(
            summary = "Browser return redirect if Stripe Connect onboarding link expires",
            description = "Public endpoint reached if user takes too long or link expires during Stripe Express onboarding."
    )
    public ResponseEntity<MessageResource> handleOnboardingRefresh() {
        return ResponseEntity.ok(new MessageResource(
                "Stripe Connect onboarding session expired or needs refresh. Please request a new onboarding link."
        ));
    }

    @GetMapping("/return")
    @Operation(
            summary = "Browser return redirect after completing Stripe Connect onboarding",
            description = "Public endpoint reached when the psychologist completes the Express onboarding flow."
    )
    public ResponseEntity<MessageResource> handleOnboardingReturn() {
        return ResponseEntity.ok(new MessageResource(
                "Stripe Connect onboarding completed successfully. Your payout account is being verified."
        ));
    }
}
