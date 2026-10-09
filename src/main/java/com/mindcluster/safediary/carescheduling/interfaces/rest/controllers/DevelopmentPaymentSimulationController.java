package com.mindcluster.safediary.carescheduling.interfaces.rest.controllers;

import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingException;
import com.mindcluster.safediary.carescheduling.interfaces.acl.CareSchedulingContextFacade;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentPaymentResultDto;
import com.mindcluster.safediary.carescheduling.interfaces.rest.resources.SimulatedPaymentResultResource;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.resources.MessageResource;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * Development only: plays the role of Payments and Payouts until it charges appointments. The bean does not exist
 * unless care-scheduling.payment-simulation.enabled=true, and it refuses requests outside the dev profile.
 */
@RestController @RequiredArgsConstructor
@ConditionalOnProperty(name="care-scheduling.payment-simulation.enabled", havingValue="true")
@RequestMapping(value="/api/v1/dev/care-scheduling/appointments", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Care Scheduling - Development payment simulation")
public class DevelopmentPaymentSimulationController {
    private final CareSchedulingContextFacade facade;
    private final Environment environment;

    @PostMapping("/{id}/payment-result") @Operation(summary="Simulate PaymentApproved or PaymentFailed; returns the appointment status")
    public ResponseEntity<?> simulate(@PathVariable Long id, @Valid @RequestBody SimulatedPaymentResultResource r) {
        if (!environment.matchesProfiles("dev & !prod"))
            throw new CareSchedulingException(new ApplicationError("FORBIDDEN", "Payment simulation is only available in dev"));
        var result = facade.applyPaymentResult(new AppointmentPaymentResultDto(id, r.paymentReference(), r.amount(), r.currency(), r.approved()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, MessageResource::new, HttpStatus.OK);
    }
}
