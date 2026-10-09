package com.mindcluster.safediary.cliniciandirectory.domain.model.queries;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
public record GetVerificationStatusQuery(DirectoryActor actor, Long clinicianId) {}
