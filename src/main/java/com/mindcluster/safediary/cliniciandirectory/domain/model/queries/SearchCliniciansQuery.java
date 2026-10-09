package com.mindcluster.safediary.cliniciandirectory.domain.model.queries;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
public record SearchCliniciansQuery(String text, String specialty, java.math.BigDecimal maxAmount, String currency, int page, int size) {}
