package com.mindcluster.safediary.iam.application.internal.commandservices;

import com.mindcluster.safediary.iam.application.commandservices.VaultCommandService;
import com.mindcluster.safediary.iam.application.internal.outboundservices.hashing.HashingService;
import com.mindcluster.safediary.iam.domain.model.aggregates.*;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AuditAction;
import com.mindcluster.safediary.iam.domain.repositories.*;
import com.mindcluster.safediary.shared.application.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import static com.mindcluster.safediary.iam.application.internal.IamPolicy.*;

/** Private vault protected by a secondary PIN (US-025). */
@Service @RequiredArgsConstructor @Transactional
public class VaultCommandServiceImpl implements VaultCommandService {
    private final PrivateVaultRepository vaults;
    private final AccountRepository accounts;
    private final AuditEventRepository audits;
    private final HashingService hashing;

    /** Five wrong PINs block the vault; every denied attempt stays in the audit trail. */
    private PrivateVault unlocked(Long accountId, String pin) {
        var now = Instant.now();
        var vault = vaults.findByAccountId(accountId).orElseThrow(() -> missing("PrivateVault", accountId));
        if (vault.isLockedAt(now)) throw rule("vault-locked", "Too many failed attempts; try again after " + vault.getLockedUntil());
        if (pin == null || !hashing.matches(pin, vault.getPinHash())) {
            vault.recordFailedAttempt(now);
            vaults.save(vault);
            audits.append(AuditEvent.record(accountId, accountId, null, AuditAction.ACCESS_DENIED, "iam", "PRIVATE_VAULT", vault.getId()));
            throw forbidden("Incorrect vault PIN");
        }
        vault.recordSuccessfulAccess();
        return vault;
    }

    public Result<PrivateVault, ApplicationError> handle(CreatePrivateVaultCommand c) {
        return attempt(() -> {
            accounts.findById(c.accountId()).filter(Account::isActive).orElseThrow(() -> unauthorized("The account is not active"));
            if (vaults.findByAccountId(c.accountId()).isPresent()) throw conflict("The private vault already exists");
            PrivateVault.validatePin(c.pin());
            return vaults.save(PrivateVault.create(c.accountId(), hashing.encode(c.pin())));
        });
    }

    public Result<PrivateVault, ApplicationError> handle(ProtectEntryInVaultCommand c) {
        return attempt(() -> {
            var vault = unlocked(c.accountId(), c.pin());
            vault.protect(c.diaryEntryId());
            return vaults.save(vault);
        });
    }

    public Result<PrivateVault, ApplicationError> handle(UnlockVaultCommand c) {
        return attempt(() -> {
            var vault = vaults.save(unlocked(c.accountId(), c.pin()));
            audits.append(AuditEvent.record(c.accountId(), c.accountId(), null, AuditAction.VAULT_UNLOCKED, "iam", "PRIVATE_VAULT", vault.getId()));
            return vault;
        });
    }
}
