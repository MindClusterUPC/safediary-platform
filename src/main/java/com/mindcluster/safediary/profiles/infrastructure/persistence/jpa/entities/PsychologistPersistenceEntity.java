package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "psychologists", schema = "profiles", uniqueConstraints = {
        @UniqueConstraint(name = "ux_psychologists_email", columnNames = {"email"})
})
@Getter
@Setter
@NoArgsConstructor
public class PsychologistPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "cost_per_session", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerSession;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "video_call_enabled", nullable = false)
    private boolean videoCallEnabled;

    @Column(name = "encrypted_chat_enabled", nullable = false)
    private boolean encryptedChatEnabled;

    @Column(name = "bio", length = 2000)
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "psychologist_categories",
            schema = "profiles",
            joinColumns = @JoinColumn(name = "psychologist_id")
    )
    @Column(name = "category_name", nullable = false, length = 100)
    private List<String> categories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "psychologist_certificates",
            schema = "profiles",
            joinColumns = @JoinColumn(name = "psychologist_id")
    )
    @Column(name = "certificate_title", nullable = false, length = 200)
    private List<String> certificateTitles = new ArrayList<>();

    @OneToMany(mappedBy = "psychologist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("dayOfWeek ASC, startTime ASC")
    private List<AvailabilitySlotPersistenceEntity> availabilitySlots = new ArrayList<>();

    public void addSlot(AvailabilitySlotPersistenceEntity slot) {
        slot.setPsychologist(this);
        availabilitySlots.add(slot);
    }

    public void clearSlots() {
        availabilitySlots.forEach(s -> s.setPsychologist(null));
        availabilitySlots.clear();
    }
}
