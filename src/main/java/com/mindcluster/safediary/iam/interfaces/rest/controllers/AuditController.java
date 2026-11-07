package com.mindcluster.safediary.iam.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.application.queryservices.AuditQueryService;
import com.mindcluster.safediary.iam.application.queryservices.ConsentQueryService;
import com.mindcluster.safediary.iam.domain.model.queries.*;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.model.AuthenticatedAccount;
import com.mindcluster.safediary.iam.interfaces.rest.resources.*;
import com.mindcluster.safediary.iam.interfaces.rest.transform.IamResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="IAM - Audit")
public class AuditController {
    private final AuditQueryService audits;
    private final ConsentQueryService consents;

    @GetMapping("/audit-events") @Operation(summary="Patient: who accessed or changed access to my information")
    public List<AuditEventResource> trail(@AuthenticationPrincipal AuthenticatedAccount me) {
        return audits.handle(new GetAuditTrailByOwnerQuery(me.accountId())).stream().map(IamResourceAssembler::audit).toList();
    }
    @GetMapping("/consent-scopes") @Operation(summary="Specialist: information each patient authorized and until when")
    public List<ConsentScopeResource> scopes(@AuthenticationPrincipal AuthenticatedAccount me) {
        return IamResourceAssembler.consents(consents.handle(new GetConsentScopeForSpecialistQuery(me.accountId())));
    }
}
