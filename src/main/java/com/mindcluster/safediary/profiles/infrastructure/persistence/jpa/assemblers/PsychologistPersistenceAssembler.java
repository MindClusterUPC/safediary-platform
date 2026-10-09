package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Psychologist;
import com.mindcluster.safediary.profiles.domain.model.entities.AvailabilitySlot;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.*;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities.AvailabilitySlotPersistenceEntity;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities.PsychologistPersistenceEntity;

import java.util.ArrayList;
import java.util.List;

public final class PsychologistPersistenceAssembler {

    private PsychologistPersistenceAssembler() {}

    public static Psychologist toDomain(PsychologistPersistenceEntity entity) {
        if (entity == null) return null;

        List<AvailabilitySlot> slots = entity.getAvailabilitySlots() != null
                ? entity.getAvailabilitySlots().stream()
                .map(s -> AvailabilitySlot.of(s.getDayOfWeek(), s.getStartTime(), s.getEndTime()))
                .toList()
                : List.of();

        return new Psychologist(
                entity.getId(),
                FullName.of(entity.getFirstName(), entity.getLastName()),
                EmailAddress.of(entity.getEmail()),
                SessionPricing.of(entity.getCostPerSession(), entity.getDurationMinutes(), entity.getCurrency()),
                PsychologistAvailability.of(slots),
                ModalityOptions.of(entity.isVideoCallEnabled(), entity.isEncryptedChatEnabled()),
                PsychologistDescription.of(entity.getBio(), entity.getCategories()),
                CertificateTitles.of(entity.getCertificateTitles())
        );
    }

    public static void copyToEntity(Psychologist domain, PsychologistPersistenceEntity entity) {
        entity.setFirstName(domain.getFullName().firstName());
        entity.setLastName(domain.getFullName().lastName());
        entity.setEmail(domain.getEmail().value());
        entity.setCostPerSession(domain.getPricing().costPerSession());
        entity.setDurationMinutes(domain.getPricing().durationMinutes());
        entity.setCurrency(domain.getPricing().currency());
        entity.setVideoCallEnabled(domain.getModalityOptions().videoCallEnabled());
        entity.setEncryptedChatEnabled(domain.getModalityOptions().encryptedChatEnabled());
        entity.setBio(domain.getDescription().bio());

        entity.setCategories(new ArrayList<>(domain.getDescription().categories()));
        entity.setCertificateTitles(new ArrayList<>(domain.getCertificateTitles().titles()));

        entity.clearSlots();
        for (AvailabilitySlot slot : domain.getAvailability().slots()) {
            AvailabilitySlotPersistenceEntity slotEntity = new AvailabilitySlotPersistenceEntity();
            slotEntity.setDayOfWeek(slot.getDayOfWeek());
            slotEntity.setStartTime(slot.getStartTime());
            slotEntity.setEndTime(slot.getEndTime());
            entity.addSlot(slotEntity);
        }
    }
}
