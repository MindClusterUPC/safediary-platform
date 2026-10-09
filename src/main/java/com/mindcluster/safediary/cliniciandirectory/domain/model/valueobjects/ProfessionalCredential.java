package com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects;

public record ProfessionalCredential(String licenseNumber, String specialty, String documentRef) {
    public ProfessionalCredential {
        if (licenseNumber == null || licenseNumber.isBlank() || licenseNumber.length() > 120
                || specialty == null || specialty.isBlank() || specialty.length() > 100
                || documentRef == null || !documentRef.matches("private://[A-Za-z0-9/_\\\\.-]{1,480}"))
            throw new IllegalArgumentException("License, specialty and a private:// document reference are required");
        licenseNumber = licenseNumber.trim();
        specialty = specialty.trim();
    }
}
