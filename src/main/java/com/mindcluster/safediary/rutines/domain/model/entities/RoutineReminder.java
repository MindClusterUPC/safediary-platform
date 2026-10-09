package com.mindcluster.safediary.rutines.domain.model.entities;

import com.mindcluster.safediary.shared.domain.model.valueobjects.SetHour;
import lombok.Getter;

import java.util.Objects;

@Getter
public class RoutineReminder {
    
    private Long id;
    private SetHour reminderTime;
    private ReminderStatus status;

    public enum ReminderStatus {
        PENDING, SNOOZED, DISMISSED, COMPLETED
    }

    public RoutineReminder(SetHour reminderTime) {
        this.reminderTime = Objects.requireNonNull(reminderTime, "reminderTime is required");
        this.status = ReminderStatus.PENDING; 
    }

    public RoutineReminder(Long id, SetHour reminderTime, ReminderStatus status) {
        this(reminderTime);
        this.id = id;
        this.status = Objects.requireNonNull(status, "status is required");
    }

    public void snooze(SetHour newTime) {
        this.reminderTime = Objects.requireNonNull(newTime, "newTime is required");
        this.status = ReminderStatus.SNOOZED;
    }

    public void dismiss() {
        this.status = ReminderStatus.DISMISSED;
    }

    public void complete() {
        this.status = ReminderStatus.COMPLETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutineReminder that = (RoutineReminder) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}