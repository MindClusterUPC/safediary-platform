package com.mindcluster.safediary.rutines.interfaces.rest;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import com.mindcluster.safediary.rutines.application.queryservices.RoutineQueryService;
import com.mindcluster.safediary.rutines.domain.model.queries.GetDailyRoutineByIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetPromptReflectionByIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetSosExerciseByIdQuery;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Restricts Rutines endpoints to the account of the bearer token. The patientId of a routine, reflection or SOS
 * exercise is the IAM account id of the patient, so a request is only accepted for the signed-in account.
 */
@Component
public class RutinesAccountGuard {

    private static final String NOT_OWNED = "The resource does not belong to the signed-in user";

    private final IamContextFacade iamContextFacade;
    private final RoutineQueryService routineQueryService;

    public RutinesAccountGuard(IamContextFacade iamContextFacade, RoutineQueryService routineQueryService) {
        this.iamContextFacade = iamContextFacade;
        this.routineQueryService = routineQueryService;
    }

    public Long currentAccountId() {
        return iamContextFacade.fetchAuthenticatedAccount()
                .map(IamAccountDto::accountId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Sign in to manage your routines"));
    }

    /** Returns the current account id, rejecting a different patient id sent by the client. */
    public Long requireOwnPatient(Long requestedPatientId) {
        var accountId = currentAccountId();
        if (requestedPatientId != null && !requestedPatientId.equals(accountId)) {
            throw new AccessDeniedException(NOT_OWNED);
        }
        return accountId;
    }

    public void requireOwnRoutine(Long routineId) {
        requireOwner(routineQueryService.handle(new GetDailyRoutineByIdQuery(routineId)).map(r -> r.getPatientId()));
    }

    public void requireOwnReflection(Long reflectionId) {
        requireOwner(routineQueryService.handle(new GetPromptReflectionByIdQuery(reflectionId)).map(r -> r.getPatientId()));
    }

    public void requireOwnSosExercise(Long exerciseId) {
        requireOwner(routineQueryService.handle(new GetSosExerciseByIdQuery(exerciseId)).map(e -> e.getPatientId()));
    }

    /** An unknown resource is left to the caller's 404; one of another account is rejected. */
    private void requireOwner(Optional<Long> ownerId) {
        var accountId = currentAccountId();
        if (ownerId.isPresent() && !ownerId.get().equals(accountId)) {
            throw new AccessDeniedException(NOT_OWNED);
        }
    }
}
