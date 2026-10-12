package com.mindcluster.safediary.iam.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.application.commandservices.ConsentCommandService;
import com.mindcluster.safediary.iam.application.queryservices.ConsentQueryService;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.iam.domain.model.queries.GetActiveConsentsByOwnerQuery;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.model.AuthenticatedAccount;
import com.mindcluster.safediary.iam.interfaces.rest.resources.*;
import com.mindcluster.safediary.iam.interfaces.rest.transform.IamResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/consents", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="IAM - Consents")
public class ConsentsController {
    private final ConsentCommandService commands;
    private final ConsentQueryService queries;

    @PostMapping @Operation(summary="Patient: share part of the history with a psychologist")
    public ResponseEntity<?> grant(@AuthenticationPrincipal AuthenticatedAccount me, @Valid @RequestBody GrantConsentResource r) {
        var permissions = r.permissions().stream().map(IamResourceAssembler::permission).toList();
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new GrantConsentCommand(me.accountId(),
                r.granteeAccountId(), permissions, r.expiresAt())), IamResourceAssembler::consent, HttpStatus.CREATED);
    }
    @GetMapping @Operation(summary="Patient: accesses in force, with specialist, scope and authorization date")
    public List<ConsentScopeResource> list(@AuthenticationPrincipal AuthenticatedAccount me) {
        return IamResourceAssembler.consents(queries.handle(new GetActiveConsentsByOwnerQuery(me.accountId())));
    }
    @PostMapping("/{id}/revocation") @Operation(summary="Patient: revoke a consent; the specialist loses access immediately")
    public ResponseEntity<?> revoke(@AuthenticationPrincipal AuthenticatedAccount me, @PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new RevokeConsentCommand(me.accountId(), id)),
                IamResourceAssembler::consent, HttpStatus.OK);
    }
}
