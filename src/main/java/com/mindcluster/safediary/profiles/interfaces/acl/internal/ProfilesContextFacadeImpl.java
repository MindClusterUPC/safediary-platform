package com.mindcluster.safediary.profiles.interfaces.acl.internal;

import com.mindcluster.safediary.profiles.application.queryservices.ProfileQueryService;
import com.mindcluster.safediary.profiles.domain.model.queries.GetPsychologistByIdQuery;
import com.mindcluster.safediary.profiles.interfaces.acl.ProfilesContextFacade;
import com.mindcluster.safediary.profiles.interfaces.acl.dto.ClinicianConsultationRateDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of ProfilesContextFacade providing ACL integration points
 * for Payments and other bounded contexts.
 */
@Service
public class ProfilesContextFacadeImpl implements ProfilesContextFacade {

    private final ProfileQueryService profileQueryService;

    public ProfilesContextFacadeImpl(ProfileQueryService profileQueryService) {
        this.profileQueryService = profileQueryService;
    }

    @Override
    public Optional<ClinicianConsultationRateDto> fetchPsychologistRate(Long psychologistId) {
        if (psychologistId == null) {
            return Optional.empty();
        }
        return profileQueryService.handle(new GetPsychologistByIdQuery(psychologistId))
                .map(p -> new ClinicianConsultationRateDto(
                        p.getId(),
                        p.getFullName().toCombinedString(),
                        p.getPricing().costPerSession(),
                        p.getPricing().durationMinutes(),
                        p.getPricing().currency(),
                        p.getModalityOptions().videoCallEnabled(),
                        p.getModalityOptions().encryptedChatEnabled()
                ));
    }

    @Override
    public boolean existsPsychologist(Long psychologistId) {
        if (psychologistId == null) {
            return false;
        }
        return profileQueryService.handle(new GetPsychologistByIdQuery(psychologistId)).isPresent();
    }
}
