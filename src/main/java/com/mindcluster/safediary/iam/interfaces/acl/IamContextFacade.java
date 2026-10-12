package com.mindcluster.safediary.iam.interfaces.acl;

import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Open Host Service of IAM. Other bounded contexts validate identity and consent through this contract instead of
 * reading IAM tables or reproducing its model.
 */
public interface IamContextFacade {
    /** Account authenticated in the current request (bearer token), if any. */
    Optional<IamAccountDto> fetchAuthenticatedAccount();

    Optional<IamAccountDto> fetchAccount(Long accountId);

    /**
     * Checks, at read time, that the patient granted the specialist an active consent covering the scope
     * (DIARY_ENTRIES, MOOD_CHECK_INS or AI_CLINICAL_SUMMARIES) and date. The decision is always audited.
     * @return the consent id when access is allowed, empty when it is denied
     */
    Optional<Long> authorizeConsentedAccess(Long patientAccountId, Long specialistAccountId, String scope, LocalDate date,
                                            String sourceContext, String resourceType, Long resourceId);
}
