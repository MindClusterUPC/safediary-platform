package com.mindcluster.safediary.carescheduling.application.queryservices;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.entities.CoordinationMessage;
import com.mindcluster.safediary.carescheduling.domain.model.queries.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.BookableSlot;
import java.util.List;
import java.util.Optional;

public interface CareSchedulingQueryService {
    List<ContactRequest> handle(GetContactRequestsQuery query);
    /** Messages of the private chat; only its two participants can read them. */
    List<CoordinationMessage> handle(GetContactRequestQuery query);
    List<Appointment> handle(GetPatientAppointmentsQuery query);
    List<Appointment> handle(GetClinicianAgendaQuery query);
    List<BookableSlot> handle(GetBookableSlotsQuery query);
    Optional<ClinicalSession> handle(GetSessionDetailsQuery query);
    Optional<CoordinationMessage> latestMessage(Long contactRequestId);
}
