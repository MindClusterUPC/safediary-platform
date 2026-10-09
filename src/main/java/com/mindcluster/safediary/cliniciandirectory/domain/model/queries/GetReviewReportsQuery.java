package com.mindcluster.safediary.cliniciandirectory.domain.model.queries;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
public record GetReviewReportsQuery(DirectoryActor actor, ReportStatus status) {}
