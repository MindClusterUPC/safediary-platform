package com.mindcluster.safediary.iam.application.internal.commandservices;

import com.mindcluster.safediary.iam.application.commandservices.ConsentCommandService;
import com.mindcluster.safediary.iam.domain.model.aggregates.*;
import com.mindcluster.safediary.iam.domain.model.commands.*;
import com.mindcluster.safediary.iam.domain.model.events.IamDomainEvent;
import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import com.mindcluster.safediary.iam.domain.repositories.*;
import com.mindcluster.safediary.shared.application.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import static com.mindcluster.safediary.iam.application.internal.IamPolicy.*;

/** Consent lifecycle (US-009, US-023) and access authorization for other contexts (US-045, TS-007). */
@Service @RequiredArgsConstructor @Transactional
public class ConsentCommandServiceImpl implements ConsentCommandService {
    private final ConsentRepository consents;
    private final AccountRepository accounts;
    private final AuditEventRepository audits;
    private final ApplicationEventPublisher events;

    private void audit(Long actor, Consent consent, AuditAction action, String source, String resourceType, Long resourceId) {
        audits.append(AuditEvent.record(actor, consent.getOwnerAccountId(), consent.getId(), action, source, resourceType, resourceId));
    }
    /** Persists and audits expirations found while reading, so the trail keeps them (US-046). */
    private void expireIfDue(Consent consent, Instant now) {
        if (consent.expireIfDue(now)) {
            consents.save(consent);
            audit(null, consent, AuditAction.CONSENT_EXPIRED, "iam", "CONSENT", consent.getId());
        }
    }

    public Result<Consent, ApplicationError> handle(GrantConsentCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            var owner = accounts.findById(c.ownerAccountId()).filter(Account::isActive)
                    .orElseThrow(() -> unauthorized("The account is not active"));
            if (owner.getRole() != AccountRole.PATIENT) throw forbidden("Only patients can share their history");
            if (c.granteeAccountId() == null) throw new IllegalArgumentException("Specialist account is required");
            var grantee = accounts.findById(c.granteeAccountId()).orElseThrow(() -> missing("Account", c.granteeAccountId()));
            if (!grantee.isActive() || grantee.getRole() != AccountRole.PSYCHOLOGIST)
                throw rule("consent-grantee", "Consents can only be granted to an active psychologist");
            for (var existing : consents.findByOwnerAccountId(owner.getId())) {
                if (!existing.getGranteeAccountId().equals(grantee.getId())) continue;
                expireIfDue(existing, now);
                if (existing.isActiveAt(now)) throw conflict("This specialist already has an active consent; revoke it to change its scope");
            }
            var saved = consents.save(Consent.grant(owner.getId(), grantee.getId(), c.permissions(), c.expiresAt(), now));
            audit(owner.getId(), saved, AuditAction.CONSENT_GRANTED, "iam", "CONSENT", saved.getId());
            events.publishEvent(new IamDomainEvent("ConsentGranted", owner.getId(), saved.getId()));
            return saved;
        });
    }

    public Result<Consent, ApplicationError> handle(RevokeConsentCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            var consent = consents.findById(c.consentId()).orElseThrow(() -> missing("Consent", c.consentId()));
            if (!consent.getOwnerAccountId().equals(c.ownerAccountId())) throw forbidden("Only the patient who granted the consent can revoke it");
            expireIfDue(consent, now);
            consent.revoke(now);
            var saved = consents.save(consent);
            audit(c.ownerAccountId(), saved, AuditAction.CONSENT_REVOKED, "iam", "CONSENT", saved.getId());
            return saved;
        });
    }

    /** Denied attempts are audited too; the transaction is not rolled back so the trail is kept. */
    public Result<Consent, ApplicationError> handle(AuthorizeConsentedAccessCommand c) {
        return attempt(() -> {
            var now = Instant.now();
            if (c.ownerAccountId() == null || c.granteeAccountId() == null || c.scope() == null
                    || c.sourceContext() == null || c.sourceContext().isBlank())
                throw new IllegalArgumentException("Owner, specialist, scope and source context are required");
            Consent matching = null, latest = null;
            for (var consent : consents.findByOwnerAccountId(c.ownerAccountId())) {
                if (!consent.getGranteeAccountId().equals(c.granteeAccountId())) continue;
                expireIfDue(consent, now);
                latest = consent;
                if (consent.covers(c.scope(), c.date(), now)) matching = consent;
            }
            if (matching != null) {
                audit(c.granteeAccountId(), matching, AuditAction.ACCESS_GRANTED, c.sourceContext(), c.resourceType(), c.resourceId());
                events.publishEvent(new IamDomainEvent("AccessAudited", c.ownerAccountId(), matching.getId()));
                return matching;
            }
            audits.append(AuditEvent.record(c.granteeAccountId(), c.ownerAccountId(), latest == null ? null : latest.getId(),
                    AuditAction.ACCESS_DENIED, c.sourceContext(), c.resourceType(), c.resourceId()));
            throw forbidden("The patient has not granted an active consent for this information");
        });
    }
}
