package com.mindcluster.safediary.iam.domain.model.entities;

import com.mindcluster.safediary.iam.domain.model.valueobjects.SharingScope;
import java.time.LocalDate;

/** Concrete scope of a consent: what kind of information and which period. Null dates leave the period open. */
public record SharingPermission(SharingScope scope, LocalDate periodStart, LocalDate periodEnd) {
    public SharingPermission {
        if (scope == null) throw new IllegalArgumentException("Sharing scope is required");
        if (periodStart != null && periodEnd != null && periodEnd.isBefore(periodStart))
            throw new IllegalArgumentException("The sharing period end must not be before its start");
    }
    public boolean covers(SharingScope requested, LocalDate date) {
        if (scope != requested) return false;
        if (date == null) return true;
        return (periodStart == null || !date.isBefore(periodStart)) && (periodEnd == null || !date.isAfter(periodEnd));
    }
}
