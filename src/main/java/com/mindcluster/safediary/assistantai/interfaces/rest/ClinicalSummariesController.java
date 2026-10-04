package com.mindcluster.safediary.assistantai.interfaces.rest;

import com.mindcluster.safediary.assistantai.application.commandservices.ClinicalSummaryCommandService;
import com.mindcluster.safediary.assistantai.application.queryservices.ClinicalSummaryQueryService;
import com.mindcluster.safediary.assistantai.domain.model.commands.GenerateWeeklyClinicalSummaryCommand;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetWeeklyClinicalSummaryQuery;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.GenerateClinicalSummaryResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.transform.ClinicalSummaryResourceFromEntityAssembler;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for weekly clinical summaries.
 * <p>
 * TODO: once IAM exists, require the active consent of the patient before a psychologist reads these summaries
 * (Event Storming, flow 3).
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/clinical-summaries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinical Summaries", description = "Weekly clinical summaries for psychologists")
public class ClinicalSummariesController {

    private final ClinicalSummaryCommandService clinicalSummaryCommandService;
    private final ClinicalSummaryQueryService clinicalSummaryQueryService;

    public ClinicalSummariesController(ClinicalSummaryCommandService clinicalSummaryCommandService,
                                       ClinicalSummaryQueryService clinicalSummaryQueryService) {
        this.clinicalSummaryCommandService = clinicalSummaryCommandService;
        this.clinicalSummaryQueryService = clinicalSummaryQueryService;
    }

    @PostMapping
    @Operation(summary = "Generate the weekly clinical summary (returns the existing one if already generated)")
    public ResponseEntity<?> generate(@Valid @RequestBody GenerateClinicalSummaryResource resource) {
        var result = clinicalSummaryCommandService.handle(new GenerateWeeklyClinicalSummaryCommand(
                resource.accountId(), resource.weekStart(), resource.locale()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                ClinicalSummaryResourceFromEntityAssembler::toResource, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get the clinical summary of a week, or the latest one")
    public ResponseEntity<?> get(@RequestParam Long accountId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return clinicalSummaryQueryService.handle(new GetWeeklyClinicalSummaryQuery(accountId, weekStart))
                .<ResponseEntity<?>>map(summary -> ResponseEntity.ok(ClinicalSummaryResourceFromEntityAssembler.toResource(summary)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("ClinicalSummary", "account " + accountId)));
    }
}
