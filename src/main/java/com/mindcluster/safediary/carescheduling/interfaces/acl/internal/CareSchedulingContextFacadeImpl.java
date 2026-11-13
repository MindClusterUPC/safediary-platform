package com.mindcluster.safediary.carescheduling.interfaces.acl.internal;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.application.queryservices.CareSchedulingQueryService;
import com.mindcluster.safediary.carescheduling.domain.model.commands.ConfirmAfterPaymentCommand;
import com.mindcluster.safediary.carescheduling.domain.model.queries.GetPayableAppointmentQuery;
import com.mindcluster.safediary.carescheduling.interfaces.acl.CareSchedulingContextFacade;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentPaymentResultDto;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.PayableAppointmentDto;
import com.mindcluster.safediary.shared.application.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class CareSchedulingContextFacadeImpl implements CareSchedulingContextFacade {
    private final CareSchedulingCommandService commands;
    private final CareSchedulingQueryService queries;

    public Optional<PayableAppointmentDto> fetchPayableAppointment(Long appointmentId, Long patientAccountId) {
        return queries.handle(new GetPayableAppointmentQuery(appointmentId, patientAccountId)).map(a ->
                new PayableAppointmentDto(a.getId(), a.getPatientAccountId(), a.getClinicianId(), a.getAmount().amount(),
                        a.getAmount().currency(), a.getPaymentReference(), a.getHold().getExpiresAt()));
    }

    public Result<String, ApplicationError> applyPaymentResult(AppointmentPaymentResultDto r) {
        if (r == null) return Result.failure(ApplicationError.validationError("payment-result", "Payment result is required"));
        return commands.handle(new ConfirmAfterPaymentCommand(r.appointmentId(), r.paymentReference(), r.amount(),
                r.currency(), r.approved())).map(appointment -> appointment.getStatus().name());
    }
}
