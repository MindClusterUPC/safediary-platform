package com.mindcluster.safediary.profiles.domain.model.valueobjects;

import java.util.Collections;
import java.util.List;

/**
 * Value Object describing a psychologist's profile with specialities/categories.
 */
public record PsychologistDescription(String bio, List<String> categories) {

    public PsychologistDescription {
        if (bio == null) {
            bio = "";
        }
        bio = bio.trim();
        if (categories == null) {
            categories = Collections.emptyList();
        } else {
            categories = categories.stream()
                    .filter(c -> c != null && !c.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
        }
    }

    public static PsychologistDescription of(String bio, List<String> categories) {
        return new PsychologistDescription(bio, categories);
    }
}
