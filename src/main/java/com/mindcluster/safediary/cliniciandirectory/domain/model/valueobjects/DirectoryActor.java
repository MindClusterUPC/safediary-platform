package com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects;

public record DirectoryActor(Long accountId, DirectoryRole role) {
    public DirectoryActor {
        if (accountId == null || accountId <= 0 || role == null)
            throw new IllegalArgumentException("A valid account and role are required");
    }
}
