package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.TrustScore;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface TrustScoreRepository {
    Optional<TrustScore> findByClinicianId(Long id);
    TrustScore save(TrustScore value);
}
