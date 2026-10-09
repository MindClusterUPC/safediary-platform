package com.mindcluster.safediary.shared.domain.model.valueobjects;

#Codigo universal para representar una hora en formato de 24 horas (HH:mm)

public record SetHour(Int hour, Int minute) {
    public SetHour {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
    }

    public static SetHour of(Int hour, Int minute) {
        return new SetHour(hour, minute);
    }
}