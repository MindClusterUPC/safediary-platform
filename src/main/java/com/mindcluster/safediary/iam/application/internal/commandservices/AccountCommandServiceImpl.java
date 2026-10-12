package com.mindcluster.safediary.iam.application.internal.commandservices;

import com.mindcluster.safediary.iam.application.commandservices.AccountCommandService;
import com.mindcluster.safediary.iam.application.commandservices.dto.*;
import com.mindcluster.safediary.iam.application.internal.outboundservices.federated.FederatedIdentityClient;
import com.mindcluster.safediary.iam.application.internal.outboundservices.hashing.HashingService;
import com.mindcluster.safediary.iam.application.internal.outboundservices.notifications.EmailNotificationClient;
import com.mindcluster.safediary.iam.application.internal.outboundservices.tokens.TokenService;
import com.mindcluster.safediary.iam.domain.model.aggregates.*;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.iam.domain.model.entities.*;
import com.mindcluster.safediary.iam.domain.model.events.IamDomainEvent;
import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import com.mindcluster.safediary.iam.domain.repositories.*;
import com.mindcluster.safediary.shared.application.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import static com.mindcluster.safediary.iam.application.internal.IamPolicy.*;

/** Account registration, sign-in (password, Google/Apple, biometric), password recovery and deletion. */
@Service @RequiredArgsConstructor @Transactional
public class AccountCommandServiceImpl implements AccountCommandService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String INVALID_CREDENTIALS = "Invalid e-mail or password";
    private static final String INVALID_CODE = "The code is invalid or expired; request a new one";

    private final AccountRepository accounts;
    private final ConsentRepository consents;
    private final AuditEventRepository audits;
    private final HashingService hashing;
    private final TokenService tokens;
    private final FederatedIdentityClient federated;
    private final EmailNotificationClient emails;
    private final ApplicationEventPublisher events;

    private AuthenticatedAccountDto startSession(Account account) {
        account.recordSuccessfulSignIn();
        var saved = accounts.save(account);
        var token = tokens.issueAccessToken(saved);
        return new AuthenticatedAccountDto(saved, token.value(), token.expiresAt());
    }
    private Account registered(Account account) {
        var saved = accounts.save(account);
        events.publishEvent(new IamDomainEvent("AccountRegistered", saved.getId(), saved.getId()));
        return saved;
    }
    private Account activeAccount(Long accountId) {
        return accounts.findById(accountId).filter(Account::isActive).orElseThrow(() -> unauthorized("The account is not active"));
    }

    // US-001: the account is created and the session starts right away.
    public Result<AuthenticatedAccountDto, ApplicationError> handle(RegisterAccountCommand c) {
        return attempt(() -> {
            var email = Account.normalizeEmail(c.email());
            Account.validatePassword(c.password());
            if (accounts.existsByEmail(email)) throw conflict("An account with this e-mail already exists");
            var role = c.role() == null ? AccountRole.PATIENT : c.role();
            return startSession(registered(Account.register(email, hashing.encode(c.password()), role)));
        });
    }

    public Result<AuthenticatedAccountDto, ApplicationError> handle(SignInCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            String email;
            try { email = Account.normalizeEmail(c.email()); } catch (IllegalArgumentException ex) { throw unauthorized(INVALID_CREDENTIALS); }
            var account = accounts.findByEmail(email).orElseThrow(() -> unauthorized(INVALID_CREDENTIALS));
            if (!account.isActive()) throw unauthorized("The account is not active");
            if (account.isLockedAt(now)) throw unauthorized("Too many failed attempts; try again after " + account.getLockedUntil());
            if (!account.hasPassword() || c.password() == null || !hashing.matches(c.password(), account.getPasswordHash())) {
                account.recordFailedSignIn(now);
                accounts.save(account);
                throw unauthorized(INVALID_CREDENTIALS);
            }
            return startSession(account);
        });
    }

    // US-003: the provider validates the identity; the account is created or recovered.
    public Result<AuthenticatedAccountDto, ApplicationError> handle(AuthenticateWithProviderCommand c) {
        return attempt(() -> {
            if (c.provider() == null || c.provider() == AuthProviderType.EMAIL || c.idToken() == null || c.idToken().isBlank())
                throw new IllegalArgumentException("Choose Google or Apple and send the provider ID token");
            var identity = federated.verify(c.provider(), c.idToken())
                    .orElseThrow(() -> unauthorized("The provider could not validate your identity"));
            var linked = accounts.findAccountIdByProvider(c.provider(), identity.externalId()).flatMap(accounts::findById);
            Account account;
            if (linked.isPresent()) account = linked.get();
            else {
                if (identity.email() == null) throw new IllegalArgumentException("The provider did not share an e-mail address");
                var existing = accounts.findByEmail(Account.normalizeEmail(identity.email()));
                if (existing.isPresent() && !identity.emailVerified())
                    throw conflict("Sign in with your password to link this provider");
                account = existing.orElseGet(() -> registered(Account.register(identity.email(), null,
                        c.role() == null ? AccountRole.PATIENT : c.role())));
                accounts.saveProvider(new AuthProvider(null, account.getId(), c.provider(), identity.externalId()));
            }
            if (!account.canSignInAt(Instant.now())) throw unauthorized("The account is not active");
            return startSession(account);
        });
    }

    // US-015: the device releases the unlock token only after FaceID or fingerprint succeeds.
    public Result<AuthenticatedAccountDto, ApplicationError> handle(SignInWithBiometricTokenCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            var unavailable = unauthorized("Biometric unlock is not available; sign in with your password");
            if (c.unlockToken() == null || c.unlockToken().isBlank()) throw unavailable;
            var token = accounts.findUnlockTokenByHash(tokens.hashOpaqueToken(c.unlockToken()))
                    .filter(t -> t.isUsableAt(now)).orElseThrow(() -> unavailable);
            var account = accounts.findById(token.getAccountId())
                    .filter(a -> a.isBiometricEnabled() && a.canSignInAt(now)).orElseThrow(() -> unavailable);
            return startSession(account);
        });
    }

    public Result<BiometricUnlockDto, ApplicationError> handle(EnableBiometricUnlockCommand c) {
        return attempt(() -> {
            var account = activeAccount(c.accountId());
            account.enableBiometricUnlock();
            accounts.save(account);
            var raw = tokens.generateOpaqueToken();
            var token = accounts.saveUnlockToken(BiometricUnlockToken.issue(account.getId(), tokens.hashOpaqueToken(raw), Instant.now()));
            return new BiometricUnlockDto(raw, token.getExpiresAt());
        });
    }

    // US-016: the answer is the same whether the e-mail exists or not.
    public Result<Void, ApplicationError> handle(RequestPasswordResetCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            accounts.findByEmail(Account.normalizeEmail(c.email())).filter(Account::isActive).filter(Account::hasPassword)
                    .ifPresent(account -> {
                        accounts.findUsableResetCodes(account.getId()).forEach(old -> { old.invalidate(now); accounts.saveResetCode(old); });
                        var code = "%06d".formatted(RANDOM.nextInt(1_000_000));
                        var saved = accounts.saveResetCode(PasswordResetCode.issue(account.getId(), hashing.encode(code), now));
                        emails.sendPasswordResetCode(account.getEmail(), code, saved.getExpiresAt());
                        events.publishEvent(new IamDomainEvent("PasswordResetRequested", account.getId(), saved.getId()));
                    });
            return null;
        });
    }

    public Result<Void, ApplicationError> handle(VerifyPasswordResetCodeCommand c) {
        return attempt(() -> { matchingCode(c.email(), c.code(), Instant.now()); return null; });
    }

    public Result<Void, ApplicationError> handle(ResetPasswordCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            Account.validatePassword(c.newPassword());
            var code = matchingCode(c.email(), c.code(), now);
            var account = accounts.findById(code.getAccountId()).orElseThrow(() -> rule("password-reset", INVALID_CODE));
            if (account.hasPassword() && hashing.matches(c.newPassword(), account.getPasswordHash()))
                throw new IllegalArgumentException("The new password must be different from the previous one");
            code.markUsed(now);
            accounts.saveResetCode(code);
            account.changePassword(hashing.encode(c.newPassword()));
            accounts.save(account);
            return null;
        });
    }

    /** Only the latest live code is accepted, and each wrong try counts against its five attempts. */
    private PasswordResetCode matchingCode(String email, String code, Instant now) {
        if (code == null || !code.matches("\\d{6}")) throw rule("password-reset", INVALID_CODE);
        var account = accounts.findByEmail(Account.normalizeEmail(email)).orElseThrow(() -> rule("password-reset", INVALID_CODE));
        var latest = accounts.findUsableResetCodes(account.getId()).stream().filter(r -> r.isUsableAt(now))
                .max(Comparator.comparing(PasswordResetCode::getExpiresAt)).orElseThrow(() -> rule("password-reset", INVALID_CODE));
        if (!hashing.matches(code, latest.getCodeHash())) {
            latest.recordFailedAttempt();
            accounts.saveResetCode(latest);
            throw rule("password-reset", INVALID_CODE);
        }
        return latest;
    }

    // US-033: sessions stop, granted accesses are revoked and the deletion date is reported.
    public Result<Account, ApplicationError> handle(RequestAccountDeletionCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            var account = activeAccount(c.accountId());
            if (account.hasPassword() && (c.password() == null || !hashing.matches(c.password(), account.getPasswordHash())))
                throw unauthorized("Confirm your identity with your password");
            account.requestDeletion(now);
            var saved = accounts.save(account);
            for (var consent : consents.findActiveInvolving(account.getId())) {
                if (consent.expireIfDue(now)) { consents.save(consent); continue; }
                consent.revoke(now);
                consents.save(consent);
                audits.append(AuditEvent.record(account.getId(), consent.getOwnerAccountId(), consent.getId(),
                        AuditAction.CONSENT_REVOKED, "iam", "CONSENT", consent.getId()));
            }
            accounts.findUnlockTokensByAccountId(account.getId()).forEach(t -> { t.revoke(now); accounts.saveUnlockToken(t); });
            return saved;
        });
    }
}
