package com.mindcluster.safediary.carescheduling.domain.repositories;

import com.mindcluster.safediary.carescheduling.domain.model.entities.AvailabilitySlot;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.AvailabilityWindow;
import java.util.List;

public interface AvailabilitySlotRepository {
    List<AvailabilitySlot> findActiveByClinicianId(Long clinicianId);
    /** Locks the clinician agenda so concurrent holds for the same clinician are serialized. */
    List<AvailabilitySlot> lockActiveByClinicianId(Long clinicianId);
    List<AvailabilitySlot> replaceActive(Long clinicianId, List<AvailabilityWindow> windows);
}
