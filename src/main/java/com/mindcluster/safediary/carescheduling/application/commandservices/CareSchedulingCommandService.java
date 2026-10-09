package com.mindcluster.safediary.carescheduling.application.commandservices;

import com.mindcluster.safediary.carescheduling.application.commandservices.dto.AuthorizedSummaryDto;
import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.commands.*;
import com.mindcluster.safediary.carescheduling.domain.model.entities.*;
import com.mindcluster.safediary.shared.application.result.*;
import java.util.List;

public interface CareSchedulingCommandService {
    Result<ContactRequest, ApplicationError> handle(SendContactRequestCommand command);
    Result<ContactRequest, ApplicationError> handle(RespondToContactRequestCommand command);
    Result<CoordinationMessage, ApplicationError> handle(SendCoordinationMessageCommand command);
    Result<List<AvailabilitySlot>, ApplicationError> handle(SetWeeklyAvailabilityCommand command);
    Result<Appointment, ApplicationError> handle(ProposeScheduleCommand command);
    Result<Appointment, ApplicationError> handle(AcceptScheduleCommand command);
    Result<Appointment, ApplicationError> handle(ConfirmAfterPaymentCommand command);
    Result<Appointment, ApplicationError> handle(CancelAppointmentCommand command);
    /** Returns how many holds expired and released their slot. */
    int handle(ExpireSlotHoldsCommand command);
    Result<ClinicalSession, ApplicationError> handle(JoinSessionCommand command);
    Result<ClinicalSession, ApplicationError> handle(CompleteSessionCommand command);
    Result<AuthorizedSummaryDto, ApplicationError> handle(RequestAuthorizedSummaryCommand command);
}
