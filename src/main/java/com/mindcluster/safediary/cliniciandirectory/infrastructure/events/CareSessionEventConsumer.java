package com.mindcluster.safediary.cliniciandirectory.infrastructure.events;

import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.ClinicianDirectoryContextFacade;
import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto.CompletedCareSessionDto;
import com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryException;
import com.mindcluster.safediary.shared.application.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class CareSessionEventConsumer {
    private final ClinicianDirectoryContextFacade facade;
    @EventListener public void onSessionCompleted(CompletedCareSessionDto event) {
        var result = facade.recordCompletedSession(event);
        if (result instanceof Result.Failure<?, ?> failure)
            throw new DirectoryException((com.mindcluster.safediary.shared.application.result.ApplicationError) failure.error());
    }
}
