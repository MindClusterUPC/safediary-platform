package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.VerificationRequest;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface VerificationRequestRepository {
    Optional<Long> findClinicianIdById(Long id);
    Optional<VerificationRequest> findById(Long id);
    Optional<VerificationRequest> findLatestByClinicianId(Long id);
    List<VerificationRequest> findPending();
    VerificationRequest save(VerificationRequest value);
}
