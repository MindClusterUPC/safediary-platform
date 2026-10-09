package com.mindcluster.safediary.cliniciandirectory.domain.model.commands;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.math.BigDecimal;
import java.util.List;
public record UpdateBannerAndRatesCommand(DirectoryActor actor, Long clinicianId, String displayName, String professionalTitle, String bio, String bannerRef, List<String> specialties, BigDecimal amount, String currency, int durationMinutes) {}
