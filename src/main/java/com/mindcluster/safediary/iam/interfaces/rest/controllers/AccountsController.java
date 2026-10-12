package com.mindcluster.safediary.iam.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.application.commandservices.AccountCommandService;
import com.mindcluster.safediary.iam.application.queryservices.AccountQueryService;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.iam.domain.model.queries.GetAccountByIdQuery;
import com.mindcluster.safediary.iam.infrastructure.authorization.sfs.model.AuthenticatedAccount;
import com.mindcluster.safediary.iam.interfaces.rest.resources.*;
import com.mindcluster.safediary.iam.interfaces.rest.transform.IamResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/accounts/me", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="IAM - Accounts")
public class AccountsController {
    private final AccountCommandService commands;
    private final AccountQueryService queries;

    @GetMapping @Operation(summary="Read the signed-in account")
    public ResponseEntity<AccountResource> me(@AuthenticationPrincipal AuthenticatedAccount me) {
        return queries.handle(new GetAccountByIdQuery(me.accountId())).map(IamResourceAssembler::account)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/biometric-unlock") @Operation(summary="Enable FaceID or fingerprint quick unlock; returns the device token once")
    public ResponseEntity<?> enableBiometricUnlock(@AuthenticationPrincipal AuthenticatedAccount me) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new EnableBiometricUnlockCommand(me.accountId())),
                IamResourceAssembler::biometric, HttpStatus.CREATED);
    }
    @PostMapping("/deletion-request") @Operation(summary="Request the deletion of the account; sessions end and granted accesses are revoked")
    public ResponseEntity<?> requestDeletion(@AuthenticationPrincipal AuthenticatedAccount me, @RequestBody DeleteAccountResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new RequestAccountDeletionCommand(me.accountId(),
                r.password())), IamResourceAssembler::account, HttpStatus.ACCEPTED);
    }
}
