package com.mindcluster.safediary.shared.domain.model.valueobjects;

public record SetHour(Integer hour, Integer minute) {
    public SetHour {
        if (hour == null || hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        if (minute == null || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
    }

    public static SetHour of(Integer hour, Integer minute) {
        return new SetHour(hour, minute);
    }
}