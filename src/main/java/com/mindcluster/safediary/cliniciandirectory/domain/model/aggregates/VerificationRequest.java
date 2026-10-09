package com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.events.DirectoryDomainEvent;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;

@Getter
public class VerificationRequest extends AbstractDomainAggregateRoot<VerificationRequest> {
    private final Long id;
    private final Long clinicianId;
    private final ProfessionalCredential credential;
    private VerificationStatus status;
    private final Instant submittedAt;
    private Long reviewedByAccountId;
    private Instant reviewedAt;
    private String rejectionReason;

    public VerificationRequest(Long id, Long clinicianId, ProfessionalCredential credential, VerificationStatus status,
            Instant submittedAt, Long reviewedByAccountId, Instant reviewedAt, String rejectionReason) {
        this.id = id; this.clinicianId = clinicianId; this.credential = credential; this.status = status;
        this.submittedAt = submittedAt; this.reviewedByAccountId = reviewedByAccountId;
        this.reviewedAt = reviewedAt; this.rejectionReason = rejectionReason;
    }

    public static VerificationRequest request(Long clinicianId, ProfessionalCredential credential) {
        var request = new VerificationRequest(null, clinicianId, credential, VerificationStatus.PENDING,
                Instant.now(), null, null, null);
        request.registerDomainEvent(new DirectoryDomainEvent("VerificationRequested", clinicianId, null));
        return request;
    }

    public void decide(VerificationStatus decision, Long reviewer, String reason) {
        if (status != VerificationStatus.PENDING) throw new IllegalArgumentException("Request has already been reviewed");
        if (decision == null || decision == VerificationStatus.PENDING)
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        if (decision == VerificationStatus.REJECTED && (reason == null || reason.isBlank() || reason.length() > 500))
            throw new IllegalArgumentException("A rejection reason is required (maximum 500 characters)");
        status = decision; reviewedByAccountId = reviewer; reviewedAt = Instant.now();
        rejectionReason = decision == VerificationStatus.REJECTED ? reason.trim() : null;
    }
}
