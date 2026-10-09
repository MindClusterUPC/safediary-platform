package com.mindcluster.safediary.carescheduling.interfaces.acl;

import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentPaymentResultDto;
import com.mindcluster.safediary.shared.application.result.*;

/** Contract that Care Scheduling offers to Payments and Payouts to apply the result of an appointment charge. */
public interface CareSchedulingContextFacade {
    /** Returns the resulting appointment status (CONFIRMED, HELD, EXPIRED, CANCELLED...). Duplicates are idempotent. */
    Result<String, ApplicationError> applyPaymentResult(AppointmentPaymentResultDto result);
}
