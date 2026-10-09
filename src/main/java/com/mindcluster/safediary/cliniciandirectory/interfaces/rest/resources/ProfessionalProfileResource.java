package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record ProfessionalProfileResource(Long id, String displayName, String professionalTitle, String bio, String bannerRef,
        List<String> specialties, ConsultationRateResource rate, VerificationStatus verificationStatus, PublicationStatus publicationStatus) {}
