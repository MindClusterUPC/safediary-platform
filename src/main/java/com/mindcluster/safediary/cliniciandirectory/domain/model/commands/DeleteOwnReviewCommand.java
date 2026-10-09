package com.mindcluster.safediary.cliniciandirectory.domain.model.commands;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.math.BigDecimal;
import java.util.List;
public record DeleteOwnReviewCommand(DirectoryActor actor, Long reviewId, boolean confirmed) {}
