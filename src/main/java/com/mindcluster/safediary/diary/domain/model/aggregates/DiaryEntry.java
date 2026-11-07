package com.mindcluster.safediary.diary.domain.model.aggregates;

import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Getter
public class DiaryEntry extends AbstractDomainAggregateRoot<DiaryEntry> {

    private Long id;
    private Long patientId;
    private String title;
    private String content;
    private boolean vaulted;
    private LocalDateTime entryDate;

    public DiaryEntry(Long patientId, String title, String content, boolean vaulted) {
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.title = (title == null || title.isBlank()) ? defaultTitle() : title.trim();
        this.content = content != null ? content : "";
        this.vaulted = vaulted;
        this.entryDate = LocalDateTime.now();
    }

    public DiaryEntry(Long id, Long patientId, String title, String content, boolean vaulted, LocalDateTime entryDate) {
        this.id = id;
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.title = (title == null || title.isBlank()) ? defaultTitle() : title.trim();
        this.content = content != null ? content : "";
        this.vaulted = vaulted;
        this.entryDate = entryDate != null ? entryDate : LocalDateTime.now();
    }

    public static String defaultTitle() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public void update(String newTitle, String newContent) {
        this.title = (newTitle == null || newTitle.isBlank()) ? defaultTitle() : newTitle.trim();
        this.content = newContent != null ? newContent : "";
    }

    public void changeVaultStatus(boolean vaulted) {
        this.vaulted = vaulted;
    }
}
