package com.mindcluster.safediary.carescheduling.domain.model.valueobjects;

/** Authenticated account; clinicianId is resolved from Clinician Directory only for psychologists. */
public record CareActor(Long accountId, CareRole role, Long clinicianId) {
    public CareActor {
        if (accountId == null || accountId <= 0 || role == null)
            throw new IllegalArgumentException("A valid account and role are required");
    }
    public CareActor(Long accountId, CareRole role) { this(accountId, role, null); }

    public CareActor withClinician(Long id) { return new CareActor(accountId, role, id); }
    public boolean isPatient() { return role == CareRole.PATIENT; }
    public boolean isPsychologist() { return role == CareRole.PSYCHOLOGIST; }
    public boolean participatesIn(Long patientAccountId, Long clinicianId) {
        if (isPatient()) return accountId.equals(patientAccountId);
        return isPsychologist() && this.clinicianId != null && this.clinicianId.equals(clinicianId);
    }
}
