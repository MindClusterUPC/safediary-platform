package com.mindcluster.safediary.cliniciandirectory.interfaces.acl.internal;

import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.ClinicianDirectoryContextFacade;
import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto.*;
import com.mindcluster.safediary.cliniciandirectory.application.commandservices.DirectoryCommandService;
import com.mindcluster.safediary.cliniciandirectory.application.queryservices.DirectoryQueryService;
import com.mindcluster.safediary.cliniciandirectory.domain.model.commands.RecordCompletedSessionCommand;
import com.mindcluster.safediary.cliniciandirectory.domain.model.queries.GetClinicianProfileQuery;
import com.mindcluster.safediary.shared.application.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class ClinicianDirectoryContextFacadeImpl implements ClinicianDirectoryContextFacade {
    private final DirectoryCommandService commands;
    private final DirectoryQueryService queries;
    public Optional<PublishedClinicianRateDto> fetchPublishedClinicianRate(Long id) {
        if (id == null || id <= 0) return Optional.empty();
        return queries.handle(new GetClinicianProfileQuery(id)).map(p -> new PublishedClinicianRateDto(
                p.getId(), p.getDisplayName(), p.getRate().amount(), p.getRate().currency(),
                p.getRate().durationMinutes(), p.getRate().version()));
    }
    public Result<Long, ApplicationError> recordCompletedSession(CompletedCareSessionDto s) {
        return commands.handle(new RecordCompletedSessionCommand(s.appointmentId(), s.clinicianId(),
                s.patientAccountId(), s.durationMinutes(), s.completedAt())).map(session -> session.id());
    }
}
