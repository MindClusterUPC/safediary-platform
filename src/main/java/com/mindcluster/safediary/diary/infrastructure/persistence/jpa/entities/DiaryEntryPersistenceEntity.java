package com.mindcluster.safediary.diary.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary_entries", schema = "diary")
@Getter
@Setter
@NoArgsConstructor
public class DiaryEntryPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_vaulted", nullable = false)
    private boolean vaulted = false;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;
}
