package com.mindcluster.safediary.profiles.domain.model.entities;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;
import lombok.Getter;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Emergency contact entity/value component for a Patient.
 * Invariant: Must contain either phone number or email address (or both).
 */
@Getter
public class EmergencyContact {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-\\(\\)]{6,20}$");

    private final String name;
    private final RelationshipType relationship;
    private final String phoneNumber;
    private final EmailAddress email;

    public EmergencyContact(String name, RelationshipType relationship, String phoneNumber, EmailAddress email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Emergency contact name must not be blank");
        }
        if (relationship == null) {
            throw new IllegalArgumentException("Emergency contact relationship type is required");
        }

        String cleanedPhone = (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber.trim() : null;
        if (cleanedPhone != null && !PHONE_PATTERN.matcher(cleanedPhone).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }

        // Invariant: At least one of phone or email must be provided
        if (cleanedPhone == null && email == null) {
            throw new IllegalArgumentException("Emergency contact must have at least a phone number or an email address");
        }

        this.name = name.trim();
        this.relationship = relationship;
        this.phoneNumber = cleanedPhone;
        this.email = email;
    }

    public static EmergencyContact of(String name, RelationshipType relationship, String phoneNumber, String emailStr) {
        EmailAddress emailVo = (emailStr != null && !emailStr.isBlank()) ? EmailAddress.of(emailStr) : null;
        return new EmergencyContact(name, relationship, phoneNumber, emailVo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmergencyContact that)) return false;
        return Objects.equals(name, that.name) &&
                relationship == that.relationship &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, relationship, phoneNumber, email);
    }
}
