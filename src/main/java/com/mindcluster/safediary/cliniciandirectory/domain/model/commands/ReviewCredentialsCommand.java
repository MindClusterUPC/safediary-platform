package com.mindcluster.safediary.cliniciandirectory.domain.model.commands;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.math.BigDecimal;
import java.util.List;
public record ReviewCredentialsCommand(DirectoryActor actor, Long requestId, VerificationStatus decision, String reason) {}
