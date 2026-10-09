package com.mindcluster.safediary.carescheduling.interfaces.rest.controllers;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.application.queryservices.CareSchedulingQueryService;
import com.mindcluster.safediary.carescheduling.domain.model.commands.CancelAppointmentCommand;
import com.mindcluster.safediary.carescheduling.domain.model.queries.GetPatientAppointmentsQuery;
import com.mindcluster.safediary.carescheduling.interfaces.rest.resources.AppointmentResource;
import com.mindcluster.safediary.carescheduling.interfaces.rest.transform.CareSchedulingResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/appointments", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Care Scheduling - Appointments")
public class AppointmentsController {
    private final CareSchedulingCommandService commands;
    private final CareSchedulingQueryService queries;
    private final IamClient iam;

    @GetMapping @Operation(summary="Patient: proposals, reservations and appointments, newest first")
    public List<AppointmentResource> list() {
        return CareSchedulingResourceAssembler.appointments(queries.handle(new GetPatientAppointmentsQuery(iam.currentActor())));
    }
    @PostMapping("/{id}/cancellation") @Operation(summary="Participant: cancel a proposal, a hold or a confirmed appointment and release the slot")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new CancelAppointmentCommand(
                iam.currentActor(), id)), CareSchedulingResourceAssembler::appointment, HttpStatus.OK);
    }
}
