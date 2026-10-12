package com.mindcluster.safediary.iam.interfaces.rest.controllers;

import com.mindcluster.safediary.iam.application.commandservices.AccountCommandService;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.iam.interfaces.rest.resources.*;
import com.mindcluster.safediary.iam.interfaces.rest.transform.IamResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.resources.MessageResource;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** Public endpoints: they start a session or recover access, so no token is required. */
@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/authentication", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="IAM - Authentication")
@SecurityRequirements
public class AuthenticationController {
    private final AccountCommandService commands;

    @PostMapping("/sign-up") @Operation(summary="Create a patient or psychologist account and start the session")
    public ResponseEntity<?> signUp(@Valid @RequestBody RegisterAccountResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new RegisterAccountCommand(r.email(), r.password(),
                r.role())), IamResourceAssembler::session, HttpStatus.CREATED);
    }
    @PostMapping("/sign-in") @Operation(summary="Sign in with e-mail and password; 5 failed attempts lock the account for 15 minutes")
    public ResponseEntity<?> signIn(@Valid @RequestBody AuthenticateResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new SignInCommand(r.email(), r.password())),
                IamResourceAssembler::session, HttpStatus.OK);
    }
    @PostMapping("/federated-sign-in") @Operation(summary="Sign up or sign in with a Google or Apple ID token")
    public ResponseEntity<?> federatedSignIn(@Valid @RequestBody FederatedAuthenticationResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new AuthenticateWithProviderCommand(r.provider(),
                r.idToken(), r.role())), IamResourceAssembler::session, HttpStatus.OK);
    }
    @PostMapping("/biometric-sign-in") @Operation(summary="Quick unlock with the token the device releases after FaceID or fingerprint")
    public ResponseEntity<?> biometricSignIn(@Valid @RequestBody BiometricAuthenticationResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new SignInWithBiometricTokenCommand(r.unlockToken())),
                IamResourceAssembler::session, HttpStatus.OK);
    }
    @PostMapping("/password-reset-requests") @Operation(summary="Send a 6-digit recovery code; the answer never reveals whether the e-mail exists")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new RequestPasswordResetCommand(r.email())),
                ignored -> new MessageResource("If the e-mail is registered, a 6-digit code was sent. It expires in 10 minutes."),
                HttpStatus.ACCEPTED);
    }
    @PostMapping("/password-reset-requests/verification") @Operation(summary="Check the 6-digit code before choosing the new password")
    public ResponseEntity<?> verifyResetCode(@Valid @RequestBody VerifyResetCodeResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new VerifyPasswordResetCodeCommand(r.email(), r.code())),
                ignored -> new MessageResource("The code is valid"), HttpStatus.OK);
    }
    @PostMapping("/password-resets") @Operation(summary="Set a new password with a valid recovery code")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordResource r) {
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new ResetPasswordCommand(r.email(), r.code(),
                r.newPassword())), ignored -> new MessageResource("The password was updated; sign in again"), HttpStatus.OK);
    }
}
