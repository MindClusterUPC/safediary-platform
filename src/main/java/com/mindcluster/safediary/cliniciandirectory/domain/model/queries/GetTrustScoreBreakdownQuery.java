package com.mindcluster.safediary.cliniciandirectory.domain.model.queries;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
public record GetTrustScoreBreakdownQuery(DirectoryActor actor, Long clinicianId) {}
