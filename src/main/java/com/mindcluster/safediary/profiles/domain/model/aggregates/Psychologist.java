package com.mindcluster.safediary.profiles.domain.model.aggregates;

import com.mindcluster.safediary.profiles.domain.model.entities.AvailabilitySlot;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Psychologist Aggregate Root.
 * Manages consultation pricing and session duration, non-overlapping weekly availability,
 * attention modalities (Video Call & Encrypted Chat), profile description with categories,
 * and certified credential titles.
 */
@Getter
public class Psychologist extends AbstractDomainAggregateRoot<Psychologist> {

    private Long id;
    private FullName fullName;
    private EmailAddress email;
    private SessionPricing pricing;
    private PsychologistAvailability availability;
    private ModalityOptions modalityOptions;
    private PsychologistDescription description;
    private CertificateTitles certificateTitles;

    public Psychologist(FullName fullName,
                        EmailAddress email,
                        SessionPricing pricing,
                        PsychologistAvailability availability,
                        ModalityOptions modalityOptions,
                        PsychologistDescription description,
                        CertificateTitles certificateTitles) {
        this.fullName = Objects.requireNonNull(fullName, "fullName is required");
        this.email = Objects.requireNonNull(email, "email is required");
        this.pricing = Objects.requireNonNull(pricing, "pricing is required");
        this.availability = availability != null ? availability : PsychologistAvailability.empty();
        this.modalityOptions = Objects.requireNonNull(modalityOptions, "modalityOptions is required");
        this.description = description != null ? description : PsychologistDescription.of("", List.of());
        this.certificateTitles = certificateTitles != null ? certificateTitles : CertificateTitles.of(List.of());
    }

    public Psychologist(Long id,
                        FullName fullName,
                        EmailAddress email,
                        SessionPricing pricing,
                        PsychologistAvailability availability,
                        ModalityOptions modalityOptions,
                        PsychologistDescription description,
                        CertificateTitles certificateTitles) {
        this(fullName, email, pricing, availability, modalityOptions, description, certificateTitles);
        this.id = id;
    }

    /**
     * Updates session pricing and duration.
     */
    public void updatePricing(BigDecimal costPerSession, int durationMinutes, String currency) {
        this.pricing = SessionPricing.of(costPerSession, durationMinutes, currency);
    }

    /**
     * Updates weekly availability schedule.
     * Enforces the domain invariant that slots must not overlap.
     *
     * @param slots new availability slots to configure
     * @throws IllegalArgumentException if any slot overlaps with another
     */
    public void configureAvailability(List<AvailabilitySlot> slots) {
        this.availability = PsychologistAvailability.of(slots);
    }

    /**
     * Updates attention modality options (Video Call & Encrypted Chat toggles).
     */
    public void updateModalityOptions(boolean videoCallEnabled, boolean encryptedChatEnabled) {
        this.modalityOptions = ModalityOptions.of(videoCallEnabled, encryptedChatEnabled);
    }

    /**
     * Updates the bio description and clinical categories.
     */
    public void updateDescription(String bio, List<String> categories) {
        this.description = PsychologistDescription.of(bio, categories);
    }

    /**
     * Updates certificate titles.
     */
    public void updateCertificateTitles(List<String> titles) {
        this.certificateTitles = CertificateTitles.of(titles);
    }

    public void updateFullName(FullName fullName) {
        this.fullName = Objects.requireNonNull(fullName, "fullName is required");
    }

    public void updateEmail(EmailAddress email) {
        this.email = Objects.requireNonNull(email, "email is required");
    }
}
