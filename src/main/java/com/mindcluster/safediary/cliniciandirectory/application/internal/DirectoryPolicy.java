package com.mindcluster.safediary.cliniciandirectory.application.internal;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.ClinicianProfile;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import java.util.Arrays;

public final class DirectoryPolicy {
    private DirectoryPolicy() {}
    public static void role(DirectoryActor actor, DirectoryRole... allowed) {
        if (actor == null) throw new DirectoryException(new ApplicationError("UNAUTHORIZED", "Sign in is required"));
        if (Arrays.stream(allowed).noneMatch(r -> r == actor.role()))
            throw new DirectoryException(new ApplicationError("FORBIDDEN", "This role cannot perform the operation"));
    }
    public static void owner(DirectoryActor actor, ClinicianProfile profile) {
        role(actor, DirectoryRole.PSYCHOLOGIST);
        if (!profile.getAccountId().equals(actor.accountId())) forbidden();
    }
    public static void forbidden() { throw new DirectoryException(new ApplicationError("FORBIDDEN", "Access to this resource is not allowed")); }
    public static DirectoryException missing(String resource, Long id) {
        return new DirectoryException(ApplicationError.notFound(resource, String.valueOf(id)));
    }
    public static DirectoryException conflict(String reason) {
        return new DirectoryException(ApplicationError.conflict("ClinicianDirectory", reason));
    }
}
