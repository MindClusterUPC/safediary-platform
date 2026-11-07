package com.mindcluster.safediary.iam.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.application.commandservices.VaultCommandService;
import com.mindcluster.safediary.iam.domain.model.commands.*;
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

@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/vault", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="IAM - Private Vault")
public class VaultController {
    private final VaultCommandService commands;

    @PostMapping @Operation(summary="Create the private vault with a 4 to 8 digit PIN")
    public ResponseEntity<?> create(@AuthenticationPrincipal AuthenticatedAccount me, @Valid @RequestBody VerifyVaultAccessResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new CreatePrivateVaultCommand(me.accountId(), r.pin())),
                IamResourceAssembler::vault, HttpStatus.CREATED);
    }
    @PostMapping("/entries") @Operation(summary="Move a diary entry into the vault after checking the PIN")
    public ResponseEntity<?> protect(@AuthenticationPrincipal AuthenticatedAccount me, @Valid @RequestBody ProtectEntryResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new ProtectEntryInVaultCommand(me.accountId(), r.pin(),
                r.diaryEntryId())), IamResourceAssembler::vault, HttpStatus.OK);
    }
    @PostMapping("/access") @Operation(summary="Open the vault; 5 wrong PINs block it for 15 minutes")
    public ResponseEntity<?> unlock(@AuthenticationPrincipal AuthenticatedAccount me, @Valid @RequestBody VerifyVaultAccessResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new UnlockVaultCommand(me.accountId(), r.pin())),
                IamResourceAssembler::vault, HttpStatus.OK);
    }
}
