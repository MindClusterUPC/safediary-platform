package com.mindcluster.safediary.profiles.domain.model.aggregates;

import com.mindcluster.safediary.profiles.domain.model.entities.EmergencyContact;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.FullName;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.PasswordHash;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.Objects;

/**
 * Patient Aggregate Root.
 * Holds personal information, credentials, and designated emergency contact.
 */
@Getter
public class Patient extends AbstractDomainAggregateRoot<Patient> {

    private Long id;
    private FullName fullName;
    private EmailAddress email;
    private PasswordHash password;
    private EmergencyContact emergencyContact;

    public Patient(FullName fullName, EmailAddress email, PasswordHash password, EmergencyContact emergencyContact) {
        this.fullName = Objects.requireNonNull(fullName, "fullName is required");
        this.email = Objects.requireNonNull(email, "email is required");
        this.password = Objects.requireNonNull(password, "password is required");
        this.emergencyContact = Objects.requireNonNull(emergencyContact, "emergencyContact is required");
    }

    public Patient(Long id, FullName fullName, EmailAddress email, PasswordHash password, EmergencyContact emergencyContact) {
        this(fullName, email, password, emergencyContact);
        this.id = id;
    }

    public void updateFullName(FullName newFullName) {
        this.fullName = Objects.requireNonNull(newFullName, "fullName cannot be null");
    }

    public void updateEmail(EmailAddress newEmail) {
        this.email = Objects.requireNonNull(newEmail, "email cannot be null");
    }

    public void updatePassword(PasswordHash newPassword) {
        this.password = Objects.requireNonNull(newPassword, "password cannot be null");
    }

    public void updateEmergencyContact(EmergencyContact newContact) {
        this.emergencyContact = Objects.requireNonNull(newContact, "emergencyContact cannot be null");
    }
}
