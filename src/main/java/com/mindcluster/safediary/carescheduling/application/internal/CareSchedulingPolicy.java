package com.mindcluster.safediary.carescheduling.application.internal;

import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.directory.ClinicianDirectoryClient;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.application.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.util.function.Supplier;

/** Resolves who takes part in scheduling and builds the errors shared by the application services. */
@Component @RequiredArgsConstructor
public class CareSchedulingPolicy {
    private final ClinicianDirectoryClient directory;

    /** Patients take part with their account; psychologists through their Clinician Directory profile. */
    public CareActor participant(CareActor actor) {
        if (actor == null) throw new CareSchedulingException(new ApplicationError("UNAUTHORIZED", "Sign in is required"));
        return switch (actor.role()) {
            case PATIENT -> actor;
            case PSYCHOLOGIST -> actor.withClinician(directory.findClinicianIdByAccountId(actor.accountId()).orElseThrow(() ->
                    forbidden("A psychologist needs a Clinician Directory profile to manage an agenda")));
            default -> throw forbidden("This role cannot take part in care scheduling");
        };
    }
    public CareActor patient(CareActor actor) {
        if (actor != null && actor.role() != CareRole.PATIENT) throw forbidden("Only patients can perform this operation");
        return participant(actor);
    }
    public CareActor psychologist(CareActor actor) {
        if (actor != null && actor.role() != CareRole.PSYCHOLOGIST) throw forbidden("Only psychologists can perform this operation");
        return participant(actor);
    }

    /** Converts domain and policy exceptions into Results and rolls back any partial change of a failed command. */
    public static <T> Result<T, ApplicationError> attempt(Supplier<T> action) {
        try { return Result.success(action.get()); }
        catch (CareSchedulingException ex) { return rollback(ex.getError()); }
        catch (IllegalArgumentException ex) { return rollback(ApplicationError.validationError("care-scheduling", ex.getMessage())); }
        catch (IllegalStateException ex) { return rollback(ApplicationError.conflict("CareScheduling", ex.getMessage())); }
    }
    private static <T> Result<T, ApplicationError> rollback(ApplicationError error) {
        try { TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); }
        catch (NoTransactionException ignored) { /* called outside a transaction */ }
        return Result.failure(error);
    }

    public static CareSchedulingException forbidden(String reason) {
        return new CareSchedulingException(new ApplicationError("FORBIDDEN", reason));
    }
    public static CareSchedulingException missing(String resource, Long id) {
        return new CareSchedulingException(ApplicationError.notFound(resource, String.valueOf(id)));
    }
    public static CareSchedulingException conflict(String reason) {
        return new CareSchedulingException(ApplicationError.conflict("CareScheduling", reason));
    }
    public static CareSchedulingException rule(String rule, String reason) {
        return new CareSchedulingException(ApplicationError.businessRuleViolation(rule, reason));
    }
}
