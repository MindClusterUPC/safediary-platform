package com.mindcluster.safediary.cliniciandirectory.domain.model.commands;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.math.BigDecimal;
import java.util.List;
public record PublishReviewCommand(DirectoryActor actor, Long clinicianId, Long appointmentId, int rating, String text) {}
