package com.mindcluster.safediary.carescheduling.interfaces.acl;

import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentPaymentResultDto;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.PayableAppointmentDto;
import com.mindcluster.safediary.shared.application.result.*;
import java.util.Optional;

/** Contract that Care Scheduling offers to Payments and Payouts to charge an appointment and apply the result. */
public interface CareSchedulingContextFacade {
    /** The patient's appointment whose slot hold is still active and waiting for payment; empty otherwise. */
    Optional<PayableAppointmentDto> fetchPayableAppointment(Long appointmentId, Long patientAccountId);

    /** Returns the resulting appointment status (CONFIRMED, HELD, EXPIRED, CANCELLED...). Duplicates are idempotent. */
    Result<String, ApplicationError> applyPaymentResult(AppointmentPaymentResultDto result);
}
