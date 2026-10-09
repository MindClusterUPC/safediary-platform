package com.mindcluster.safediary.rutines.domain.model.aggregates;

import com.mindcluster.safediary.rutines.domain.model.entities.RoutineReminder;
import com.mindcluster.safediary.rutines.domain.model.events.DailyRoutineCreatedEvent;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.NotificationStatus;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineTitle;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.mindcluster.safediary.shared.domain.model.valueobjects.SetHour;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class DailyRoutine extends AbstractDomainAggregateRoot<DailyRoutine> {

    private Long id;
    private Long patientId;
    private RoutineTitle title;
    private Set<FrequencyDays> frequency;
    private NotificationStatus notificationStatus;
    private boolean isActive;

    private List<RoutineReminder> reminders = new ArrayList<>();

    public DailyRoutine(Long patientId, RoutineTitle title, Set<FrequencyDays> frequency, NotificationStatus notificationStatus) {
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.title = Objects.requireNonNull(title, "title is required");
        this.frequency = Objects.requireNonNull(frequency, "frequency is required");
        if (this.frequency.isEmpty()) {
            throw new IllegalArgumentException("At least one frequency day is required");
        }
        this.notificationStatus = Objects.requireNonNull(notificationStatus, "notificationStatus is required");
        this.isActive = true;
        this.reminders = new ArrayList<>();

        this.registerEvent(new DailyRoutineCreatedEvent(
                null,
                this.patientId,
                this.title.value(),
                this.frequency.stream().map(Enum::name).collect(Collectors.toSet()),
                this.notificationStatus == NotificationStatus.ENABLED
        ));
    }

    public DailyRoutine(RoutineTitle title, Set<FrequencyDays> frequency, NotificationStatus notificationStatus) {
        this(null, null, title, frequency, notificationStatus, true);
    }

    public DailyRoutine(Long id, Long patientId, RoutineTitle title, Set<FrequencyDays> frequency, NotificationStatus notificationStatus, boolean isActive) {
        this.id = id;
        this.patientId = patientId;
        this.title = Objects.requireNonNull(title, "title is required");
        this.frequency = Objects.requireNonNull(frequency, "frequency is required");
        this.notificationStatus = Objects.requireNonNull(notificationStatus, "notificationStatus is required");
        this.isActive = isActive;
        this.reminders = new ArrayList<>();
    }

    public DailyRoutine(Long id, Long patientId, RoutineTitle title, Set<FrequencyDays> frequency, NotificationStatus notificationStatus, boolean isActive, List<RoutineReminder> reminders) {
        this(id, patientId, title, frequency, notificationStatus, isActive);
        if (reminders != null) {
            this.reminders = new ArrayList<>(reminders);
        }
    }

    public void updateTitle(RoutineTitle newTitle) {
        this.title = Objects.requireNonNull(newTitle, "newTitle is required");
    }

    public void changeNotificationStatus(NotificationStatus newStatus) {
        this.notificationStatus = Objects.requireNonNull(newStatus, "newStatus is required");
    }

    public void deactivateRoutine() {
        this.isActive = false;
    }

    public void activateRoutine() {
        this.isActive = true;
    }

    public void addReminder(SetHour time) {
        Objects.requireNonNull(time, "time is required");
        RoutineReminder newReminder = new RoutineReminder(time);
        this.reminders.add(newReminder);
    }

    public void dismissReminder(Long reminderId) {
        Objects.requireNonNull(reminderId, "reminderId is required");
        this.reminders.stream()
                .filter(r -> reminderId.equals(r.getId()))
                .findFirst()
                .ifPresent(RoutineReminder::dismiss);
    }

    public Set<FrequencyDays> getFrequency() {
        return Collections.unmodifiableSet(frequency);
    }

    public List<RoutineReminder> getReminders() {
        return Collections.unmodifiableList(reminders);
    }
}