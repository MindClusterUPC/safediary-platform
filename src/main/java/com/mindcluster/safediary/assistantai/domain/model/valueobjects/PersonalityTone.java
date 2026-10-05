package com.mindcluster.safediary.assistantai.domain.model.valueobjects;

import java.util.Arrays;
import java.util.Optional;

/**
 * Linguistic style the AI companion (Diarito) uses when answering, shown to users as a named personality.
 */
public enum PersonalityTone {
    EMPATHIC("Sol"),
    REFLECTIVE("Luma"),
    ANALYTICAL("Kai"),
    CALM("Nara");

    private final String personalityName;

    PersonalityTone(String personalityName) {
        this.personalityName = personalityName;
    }

    public String personalityName() {
        return personalityName;
    }

    /**
     * Resolves a tone from its personality name (Sol, Luma...) or its enum name (EMPATHIC...), ignoring case.
     */
    public static Optional<PersonalityTone> fromName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(name) || t.personalityName.equalsIgnoreCase(name))
                .findFirst();
    }
}
