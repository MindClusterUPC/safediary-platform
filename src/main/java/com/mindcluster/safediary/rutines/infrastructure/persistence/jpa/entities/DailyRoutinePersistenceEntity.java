package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.NotificationStatus;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "daily_routines", schema = "rutines")
@Getter
@Setter
@NoArgsConstructor
public class DailyRoutinePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "title", nullable = false, length = 60)
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "daily_routine_frequencies",
            schema = "rutines",
            joinColumns = @JoinColumn(name = "routine_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_day", nullable = false, length = 20)
    private Set<FrequencyDays> frequency = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 20)
    private NotificationStatus notificationStatus;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}