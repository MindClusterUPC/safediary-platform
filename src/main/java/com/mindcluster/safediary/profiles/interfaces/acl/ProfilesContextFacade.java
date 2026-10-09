package com.mindcluster.safediary.profiles.interfaces.acl;

import com.mindcluster.safediary.profiles.interfaces.acl.dto.ClinicianConsultationRateDto;

import java.util.Optional;

/**
 * ACL Facade interface exposed by the Profiles Bounded Context.
 * Other bounded contexts (such as Payments, Care Scheduling) consume this facade
 * to retrieve clinician consultation rates without coupling to internal models or tables.
 */
public interface ProfilesContextFacade {

    /**
     * Retrieves consultation rate and pricing details for a given psychologist.
     *
     * @param psychologistId the specialist identifier
     * @return the consultation rate DTO if the psychologist exists
     */
    Optional<ClinicianConsultationRateDto> fetchPsychologistRate(Long psychologistId);

    /**
     * Checks if a psychologist exists and is available for consultations.
     *
     * @param psychologistId the specialist identifier
     * @return true if the psychologist exists
     */
    boolean existsPsychologist(Long psychologistId);
}
