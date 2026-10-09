package com.mindcluster.safediary.carescheduling.domain.model.aggregates;

import com.mindcluster.safediary.carescheduling.domain.model.entities.CoordinationMessage;
import com.mindcluster.safediary.carescheduling.domain.model.events.CareSchedulingDomainEvent;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;
import java.util.Objects;

/** Private coordination between a patient and a psychologist. It is not an appointment and grants no diary access. */
@Getter
public class ContactRequest extends AbstractDomainAggregateRoot<ContactRequest> {
    private final Long id;
    private final Long patientAccountId;
    private final Long clinicianId;
    private ContactRequestStatus status;
    private final Instant createdAt;

    public ContactRequest(Long id, Long patientAccountId, Long clinicianId, ContactRequestStatus status, Instant createdAt) {
        if (patientAccountId == null || patientAccountId <= 0 || clinicianId == null || clinicianId <= 0)
            throw new IllegalArgumentException("A contact request requires a patient and a clinician");
        this.id = id;
        this.patientAccountId = patientAccountId;
        this.clinicianId = clinicianId;
        this.status = Objects.requireNonNull(status, "Status is required");
        this.createdAt = createdAt;
    }

    public static ContactRequest open(Long patientAccountId, Long clinicianId) {
        return new ContactRequest(null, patientAccountId, clinicianId, ContactRequestStatus.PENDING, null);
    }

    public boolean isOpen() { return status == ContactRequestStatus.PENDING || status == ContactRequestStatus.ACCEPTED; }

    public boolean hasParticipant(CareActor participant) {
        return participant.participatesIn(patientAccountId, clinicianId);
    }

    /** Opening the chat, replying or proposing a schedule means the psychologist accepted the contact. */
    public void accept() {
        if (status == ContactRequestStatus.ACCEPTED) return;
        if (status != ContactRequestStatus.PENDING) throw new IllegalStateException("The contact request is no longer open");
        status = ContactRequestStatus.ACCEPTED;
        registerDomainEvent(new CareSchedulingDomainEvent("ContactRequestAccepted", id, null));
    }

    public void reject() {
        if (status != ContactRequestStatus.PENDING) throw new IllegalStateException("Only pending contact requests can be rejected");
        status = ContactRequestStatus.REJECTED;
        registerDomainEvent(new CareSchedulingDomainEvent("ContactRequestRejected", id, null));
    }

    public CoordinationMessage sendMessage(CareActor sender, String body, Instant now) {
        if (!isOpen()) throw new IllegalStateException("Messages can only be sent while the contact request is open");
        if (!hasParticipant(sender)) throw new IllegalArgumentException("Only the patient and the psychologist can write in this chat");
        if (sender.isPsychologist()) accept();
        return new CoordinationMessage(null, id, sender.accountId(), body, now);
    }
}
