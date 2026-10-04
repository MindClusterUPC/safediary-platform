package com.mindcluster.safediary.assistantai.interfaces.rest;

import com.mindcluster.safediary.assistantai.application.queryservices.CrisisQueryService;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetCurrentRiskAssessmentQuery;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.CrisisHotlineResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.transform.CrisisHotlineResourceFromValueObjectAssembler;
import com.mindcluster.safediary.assistantai.interfaces.rest.transform.RiskAssessmentResourceFromEntityAssembler;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for crisis resources and session risk state.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Crisis Alerts", description = "Emergency hotlines and risk assessments")
public class CrisisAlertController {

    private final CrisisQueryService crisisQueryService;

    public CrisisAlertController(CrisisQueryService crisisQueryService) {
        this.crisisQueryService = crisisQueryService;
    }

    @GetMapping("/crisis-resources")
    @Operation(summary = "Get the emergency hotlines")
    public ResponseEntity<List<CrisisHotlineResource>> getCrisisResources() {
        return ResponseEntity.ok(crisisQueryService.getCrisisHotlines().stream()
                .map(CrisisHotlineResourceFromValueObjectAssembler::toResource)
                .toList());
    }

    @GetMapping("/conversation-sessions/{sessionId}/risk-assessments/latest")
    @Operation(summary = "Get the latest risk assessment of a session")
    public ResponseEntity<?> getLatestRiskAssessment(@PathVariable Long sessionId) {
        return crisisQueryService.handle(new GetCurrentRiskAssessmentQuery(sessionId))
                .<ResponseEntity<?>>map(assessment -> ResponseEntity.ok(RiskAssessmentResourceFromEntityAssembler.toResource(assessment)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("RiskAssessment", "session " + sessionId)));
    }
}
