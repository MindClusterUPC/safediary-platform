package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients", schema = "profiles", uniqueConstraints = {
        @UniqueConstraint(name = "ux_patients_email", columnNames = {"email"})
})
@Getter
@Setter
@NoArgsConstructor
public class PatientPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "emergency_contact_name", nullable = false, length = 120)
    private String emergencyContactName;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_contact_relationship", nullable = false, length = 30)
    private RelationshipType emergencyContactRelationship;

    @Column(name = "emergency_contact_phone", length = 30)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_email", length = 254)
    private String emergencyContactEmail;
}
