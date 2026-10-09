package com.mindcluster.safediary.rutines.domain.model.aggregates;

import com.mindcluster.safediary.rutines.domain.model.events.DailyReflectionSubmittedEvent;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.ReflectionAnswer;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;


@Getter
public class PromptReflection extends AbstractDomainAggregateRoot<PromptReflection> {

    private Long id;
    private Long patientId; 
    private String promptText; 
    private ReflectionAnswer answer;
    private ReflectionStatus status;
    private LocalDateTime submittedAt;

    public enum ReflectionStatus {
        PENDING,
        SUBMITTED
    }

    public PromptReflection(Long patientId, String promptText) {
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.promptText = Objects.requireNonNull(promptText, "promptText is required");
        this.status = ReflectionStatus.PENDING;
    }

    public PromptReflection(Long id, Long patientId, String promptText, ReflectionAnswer answer, ReflectionStatus status, LocalDateTime submittedAt) {
        this(patientId, promptText);
        this.id = id;
        this.answer = answer;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public void submit(ReflectionAnswer submittedAnswer) {
        if (this.status == ReflectionStatus.SUBMITTED) {
            throw new IllegalStateException("This reflection has already been submitted and cannot be modified.");
        }
        
        this.answer = Objects.requireNonNull(submittedAnswer, "A valid reflection answer must be provided.");
        this.status = ReflectionStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();

        this.registerEvent(new DailyReflectionSubmittedEvent(this.id, this.patientId, this.promptText, this.answer.answer()));
    }
}