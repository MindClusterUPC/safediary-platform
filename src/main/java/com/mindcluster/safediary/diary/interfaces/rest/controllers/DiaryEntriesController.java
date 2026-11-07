package com.mindcluster.safediary.diary.interfaces.rest.controllers;

import com.mindcluster.safediary.diary.application.commandservices.DiaryEntryCommandService;
import com.mindcluster.safediary.diary.application.queryservices.DiaryEntryQueryService;
import com.mindcluster.safediary.diary.domain.model.commands.CreateDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.model.commands.DeleteDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.model.commands.UpdateDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.model.queries.GetAllDiaryEntriesByPatientIdQuery;
import com.mindcluster.safediary.diary.domain.model.queries.GetDiaryEntryByIdQuery;
import com.mindcluster.safediary.diary.interfaces.rest.resources.CreateDiaryEntryResource;
import com.mindcluster.safediary.diary.interfaces.rest.resources.DiaryEntryResponseResource;
import com.mindcluster.safediary.diary.interfaces.rest.resources.UpdateDiaryEntryResource;
import com.mindcluster.safediary.diary.interfaces.rest.transform.DiaryEntryResourceAssembler;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/diary-entries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Diary Entries", description = "Personal Markdown Diary Entries Management")
public class DiaryEntriesController {

    private final DiaryEntryCommandService commandService;
    private final DiaryEntryQueryService queryService;
    private final IamContextFacade iamContextFacade;

    public DiaryEntriesController(DiaryEntryCommandService commandService,
                                  DiaryEntryQueryService queryService,
                                  IamContextFacade iamContextFacade) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.iamContextFacade = iamContextFacade;
    }

    private Long resolvePatientAccountId(HttpServletRequest request, Long payloadPatientId) {
        if (payloadPatientId != null && payloadPatientId > 0) {
            return payloadPatientId;
        }
        return iamContextFacade.fetchAuthenticatedAccount()
                .map(acc -> acc.accountId())
                .orElseGet(() -> {
                    String header = request.getHeader("X-Account-Id");
                    if (header != null && !header.isBlank()) {
                        try {
                            return Long.parseLong(header.trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    return 1L;
                });
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new personal diary entry (Markdown)")
    public ResponseEntity<?> createEntry(@Valid @RequestBody CreateDiaryEntryResource resource,
                                         HttpServletRequest request) {
        Long patientId = resolvePatientAccountId(request, resource.patientId());
        boolean isVaulted = resource.vaulted() != null && resource.vaulted();
        var command = new CreateDiaryEntryCommand(
                patientId,
                resource.title(),
                resource.content(),
                isVaulted
        );
        var result = commandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DiaryEntryResourceAssembler::toResource,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "List all diary entries for the authenticated patient")
    public ResponseEntity<List<DiaryEntryResponseResource>> getEntries(HttpServletRequest request,
                                                                       @RequestParam(required = false) Long patientId) {
        Long resolvedPatientId = resolvePatientAccountId(request, patientId);
        var entries = queryService.handle(new GetAllDiaryEntriesByPatientIdQuery(resolvedPatientId)).stream()
                .map(DiaryEntryResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a diary entry by ID")
    public ResponseEntity<DiaryEntryResponseResource> getEntryById(@PathVariable Long id) {
        return queryService.handle(new GetDiaryEntryByIdQuery(id))
                .map(DiaryEntryResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update title and Markdown content of a diary entry")
    public ResponseEntity<?> updateEntry(@PathVariable Long id,
                                         @Valid @RequestBody UpdateDiaryEntryResource resource,
                                         HttpServletRequest request) {
        Long patientId = resolvePatientAccountId(request, null);
        var command = new UpdateDiaryEntryCommand(
                id,
                patientId,
                resource.title(),
                resource.content()
        );
        var result = commandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                DiaryEntryResourceAssembler::toResource,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a diary entry by ID")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id,
                                         HttpServletRequest request) {
        Long patientId = resolvePatientAccountId(request, null);
        var command = new DeleteDiaryEntryCommand(id, patientId);
        var result = commandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ignored -> ResponseEntity.noContent().build(),
                HttpStatus.NO_CONTENT
        );
    }
}
