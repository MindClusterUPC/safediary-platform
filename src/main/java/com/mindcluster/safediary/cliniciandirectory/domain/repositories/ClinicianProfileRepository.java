package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.ClinicianProfile;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface ClinicianProfileRepository {
    Optional<ClinicianProfile> findById(Long id);
    Optional<ClinicianProfile> findByIdForUpdate(Long id);
    Optional<ClinicianProfile> findByAccountId(Long accountId);
    List<ClinicianProfile> findPublicProfiles();
    ClinicianProfile save(ClinicianProfile value);
}
