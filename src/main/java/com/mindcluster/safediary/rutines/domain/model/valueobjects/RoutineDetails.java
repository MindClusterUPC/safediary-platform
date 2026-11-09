package com.mindcluster.safediary.rutines.domain.model.valueobjects;

public record RoutineDetails(String description, String time, Integer minutes, String kind) {
    public RoutineDetails {
        description = description == null ? "" : description.trim();
        time = time == null ? "09:00" : time;
        minutes = minutes == null ? 5 : minutes;
        kind = kind == null ? "GROUNDING" : kind;
        if (description.length() > 1000 || !time.matches("([01][0-9]|2[0-3]):[0-5][0-9]")
                || minutes < 1 || minutes > 60 || !(kind.equals("BREATHING") || kind.equals("GROUNDING")))
            throw new IllegalArgumentException("Invalid routine description, time, duration or exercise type");
    }
}
