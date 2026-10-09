package com.mindcluster.safediary.profiles.interfaces.rest.transform;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.PsychologistResponseResource;

public final class PsychologistResourceAssembler {

    private PsychologistResourceAssembler() {}

    public static PsychologistResponseResource toResource(Psychologist psychologist) {
        if (psychologist == null) return null;

        var pricing = new PsychologistResponseResource.SessionPricingResource(
                psychologist.getPricing().costPerSession(),
                psychologist.getPricing().durationMinutes(),
                psychologist.getPricing().currency()
        );

        var modalities = new PsychologistResponseResource.ModalityOptionsResource(
                psychologist.getModalityOptions().videoCallEnabled(),
                psychologist.getModalityOptions().encryptedChatEnabled()
        );

        var availability = psychologist.getAvailability().slots().stream()
                .map(slot -> new PsychologistResponseResource.AvailabilitySlotResponseResource(
                        slot.getDayOfWeek(),
                        slot.getStartTime(),
                        slot.getEndTime()
                ))
                .toList();

        return new PsychologistResponseResource(
                psychologist.getId(),
                psychologist.getFullName().firstName(),
                psychologist.getFullName().lastName(),
                psychologist.getFullName().toCombinedString(),
                psychologist.getEmail().value(),
                pricing,
                modalities,
                psychologist.getDescription().bio(),
                psychologist.getDescription().categories(),
                psychologist.getCertificateTitles().titles(),
                availability
        );
    }
}
