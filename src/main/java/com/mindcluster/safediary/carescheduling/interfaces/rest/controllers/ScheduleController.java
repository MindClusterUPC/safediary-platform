package com.mindcluster.safediary.carescheduling.interfaces.rest.controllers;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.application.queryservices.CareSchedulingQueryService;
import com.mindcluster.safediary.carescheduling.domain.model.commands.*;
import com.mindcluster.safediary.carescheduling.domain.model.queries.*;
import com.mindcluster.safediary.carescheduling.interfaces.rest.resources.*;
import com.mindcluster.safediary.carescheduling.interfaces.rest.transform.CareSchedulingResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/schedule", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Care Scheduling - Schedule")
public class ScheduleController {
    private final CareSchedulingCommandService commands;
    private final CareSchedulingQueryService queries;
    private final IamClient iam;

    @PutMapping("/availability") @Operation(summary="Psychologist: replace the weekly bookable availability (agenda timezone)")
    public ResponseEntity<?> setAvailability(@RequestBody @Size(max=50) List<@Valid AvailabilityWindowResource> windows) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new SetWeeklyAvailabilityCommand(iam.currentActor(),
                windows.stream().map(CareSchedulingResourceAssembler::window).toList())),
                slots -> slots.stream().map(CareSchedulingResourceAssembler::availability).toList(), HttpStatus.OK);
    }
    @GetMapping("/clinicians/{clinicianId}/slots") @Operation(summary="Bookable slots of one day; only the agenda owner sees held and confirmed slots")
    public List<BookableSlotResource> slots(@PathVariable Long clinicianId,
                                            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date) {
        return queries.handle(new GetBookableSlotsQuery(iam.currentActor(), clinicianId, date)).stream()
                .map(CareSchedulingResourceAssembler::slot).toList();
    }
    @GetMapping("/agenda") @Operation(summary="Psychologist: professional agenda; defaults to the next 7 days")
    public List<AppointmentResource> agenda(@RequestParam(required=false) Instant from, @RequestParam(required=false) Instant to) {
        return CareSchedulingResourceAssembler.appointments(queries.handle(new GetClinicianAgendaQuery(iam.currentActor(), from, to)));
    }
    @PostMapping("/proposals") @Operation(summary="Psychologist: propose a free slot; no reservation or charge is created")
    public ResponseEntity<?> propose(@Valid @RequestBody ProposeScheduleResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new ProposeScheduleCommand(iam.currentActor(),
                r.contactRequestId(), r.startsAt())), CareSchedulingResourceAssembler::appointment, HttpStatus.CREATED);
    }
    @PostMapping("/proposals/{appointmentId}/acceptance") @Operation(summary="Patient: accept, hold the slot for one hour and request the charge")
    public ResponseEntity<?> accept(@PathVariable Long appointmentId) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new AcceptScheduleCommand(iam.currentActor(),
                appointmentId)), CareSchedulingResourceAssembler::appointment, HttpStatus.OK);
    }
}
