package com.mindcluster.safediary.cliniciandirectory.interfaces.acl;

import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto.*;
import com.mindcluster.safediary.shared.application.result.*;
import java.util.Optional;
public interface ClinicianDirectoryContextFacade {
    Optional<PublishedClinicianRateDto> fetchPublishedClinicianRate(Long clinicianId);
    Result<Long, ApplicationError> recordCompletedSession(CompletedCareSessionDto session);
}
