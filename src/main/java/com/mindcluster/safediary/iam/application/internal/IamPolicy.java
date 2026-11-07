package com.mindcluster.safediary.iam.application.internal;

import com.mindcluster.safediary.shared.application.result.*;
import java.util.function.Supplier;

/**
 * Error helpers shared by the IAM application services. Failures do not roll back on purpose: failed sign-ins,
 * wrong codes and denied accesses must stay recorded (lockouts and audit trail).
 */
public final class IamPolicy {
    private IamPolicy() {}

    public static <T> Result<T, ApplicationError> attempt(Supplier<T> action) {
        try { return Result.success(action.get()); }
        catch (IamException ex) { return Result.failure(ex.getError()); }
        catch (IllegalArgumentException ex) { return Result.failure(ApplicationError.validationError("iam", ex.getMessage())); }
        catch (IllegalStateException ex) { return Result.failure(ApplicationError.conflict("Iam", ex.getMessage())); }
    }

    public static IamException unauthorized(String reason) { return new IamException(new ApplicationError("UNAUTHORIZED", reason)); }
    public static IamException forbidden(String reason) { return new IamException(new ApplicationError("FORBIDDEN", reason)); }
    public static IamException missing(String resource, Object id) {
        return new IamException(ApplicationError.notFound(resource, String.valueOf(id)));
    }
    public static IamException conflict(String reason) { return new IamException(ApplicationError.conflict("Iam", reason)); }
    public static IamException rule(String rule, String reason) {
        return new IamException(ApplicationError.businessRuleViolation(rule, reason));
    }
}
