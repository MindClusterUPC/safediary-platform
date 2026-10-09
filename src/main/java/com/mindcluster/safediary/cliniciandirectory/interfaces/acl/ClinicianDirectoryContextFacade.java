package com.mindcluster.safediary.cliniciandirectory.interfaces.acl;

import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto.*;
import com.mindcluster.safediary.shared.application.result.*;
import java.util.Optional;
public interface ClinicianDirectoryContextFacade {
    Optional<PublishedClinicianRateDto> fetchPublishedClinicianRate(Long clinicianId);
    /** Resolves the clinician profile owned by an IAM account, in any verification or publication state. */
    Optional<Long> fetchClinicianIdByAccountId(Long accountId);
    Result<Long, ApplicationError> recordCompletedSession(CompletedCareSessionDto session);
}
