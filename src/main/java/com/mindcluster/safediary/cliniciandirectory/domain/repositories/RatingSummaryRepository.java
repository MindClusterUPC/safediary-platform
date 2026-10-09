package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.RatingSummary;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface RatingSummaryRepository {
    Optional<RatingSummary> findByClinicianId(Long id);
    RatingSummary save(RatingSummary value);
}
