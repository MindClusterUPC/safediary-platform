package com.mindcluster.safediary.profiles.domain.model.valueobjects;

import com.mindcluster.safediary.profiles.domain.model.entities.AvailabilitySlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value Object managing a psychologist's schedule availability.
 * Enforces the domain rule: no two slots on the same day can overlap.
 */
public record PsychologistAvailability(List<AvailabilitySlot> slots) {

    public PsychologistAvailability {
        if (slots == null) {
            slots = Collections.emptyList();
        } else {
            validateNoOverlaps(slots);
            slots = List.copyOf(slots);
        }
    }

    public static PsychologistAvailability of(List<AvailabilitySlot> slots) {
        return new PsychologistAvailability(slots);
    }

    public static PsychologistAvailability empty() {
        return new PsychologistAvailability(Collections.emptyList());
    }

    private static void validateNoOverlaps(List<AvailabilitySlot> slotList) {
        for (int i = 0; i < slotList.size(); i++) {
            AvailabilitySlot first = slotList.get(i);
            for (int j = i + 1; j < slotList.size(); j++) {
                AvailabilitySlot second = slotList.get(j);
                if (first.overlapsWith(second)) {
                    throw new IllegalArgumentException(
                            "Overlapping availability slots detected: " + first + " overlaps with " + second
                    );
                }
            }
        }
    }
}
