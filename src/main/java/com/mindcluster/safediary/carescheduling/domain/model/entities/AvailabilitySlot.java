package com.mindcluster.safediary.carescheduling.domain.model.entities;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.AvailabilityWindow;

public record AvailabilitySlot(Long id, Long clinicianId, AvailabilityWindow window, boolean active) {
    public AvailabilitySlot {
        if (clinicianId == null || clinicianId <= 0 || window == null)
            throw new IllegalArgumentException("Availability requires a clinician and a window");
    }
}
