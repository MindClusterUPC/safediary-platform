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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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

    /**
     * Diary entries are private: they always belong to the account of the bearer token. A patient id sent by the
     * client is only accepted when it is that same account.
     */
    private Long resolvePatientAccountId(HttpServletRequest request, Long payloadPatientId) {
        Long accountId = iamContextFacade.fetchAuthenticatedAccount()
                .map(acc -> acc.accountId())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to use your diary"));
        if (payloadPatientId != null && payloadPatientId > 0 && !payloadPatientId.equals(accountId)) {
            throw new AccessDeniedException("The diary does not belong to the signed-in user");
        }
        return accountId;
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
    public ResponseEntity<DiaryEntryResponseResource> getEntryById(@PathVariable Long id, HttpServletRequest request) {
        Long patientId = resolvePatientAccountId(request, null);
        return queryService.handle(new GetDiaryEntryByIdQuery(id))
                .filter(entry -> entry.getPatientId().equals(patientId))
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
