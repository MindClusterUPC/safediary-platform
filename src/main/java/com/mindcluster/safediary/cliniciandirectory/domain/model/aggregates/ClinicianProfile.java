package com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.events.DirectoryDomainEvent;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.net.URI;
import java.util.List;
import java.util.Locale;

@Getter
public class ClinicianProfile extends AbstractDomainAggregateRoot<ClinicianProfile> {
    private final Long id;
    private final Long accountId;
    private String displayName;
    private String professionalTitle;
    private String bio;
    private String bannerRef;
    private List<String> specialties;
    private ConsultationRate rate;
    private VerificationStatus verificationStatus;
    private PublicationStatus publicationStatus;

    public ClinicianProfile(Long id, Long accountId, String displayName, String professionalTitle,
                            String bio, String bannerRef, List<String> specialties, ConsultationRate rate,
                            VerificationStatus verificationStatus, PublicationStatus publicationStatus) {
        if (accountId == null || accountId <= 0) throw new IllegalArgumentException("Account is required");
        this.id = id;
        this.accountId = accountId;
        setDetails(displayName, professionalTitle, bio, bannerRef, specialties);
        this.rate = java.util.Objects.requireNonNull(rate, "Rate is required");
        this.verificationStatus = java.util.Objects.requireNonNull(verificationStatus);
        this.publicationStatus = java.util.Objects.requireNonNull(publicationStatus);
    }

    private void setDetails(String name, String title, String description, String banner, List<String> areas) {
        if (name == null || name.isBlank() || name.length() > 160
                || title == null || title.isBlank() || title.length() > 120
                || description == null || description.isBlank() || description.length() > 2000
                || areas == null || areas.isEmpty() || areas.size() > 20
                || areas.stream().anyMatch(s -> s == null || s.isBlank() || s.length() > 100))
            throw new IllegalArgumentException("Name, professional title, bio and specialties are required");
        if (banner != null && !banner.isBlank()) {
            URI uri = URI.create(banner);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || banner.length() > 512)
                throw new IllegalArgumentException("Banner must be a public HTTPS URL");
        }
        this.displayName = name.trim();
        this.professionalTitle = title.trim();
        this.bio = description.trim();
        this.bannerRef = banner == null || banner.isBlank() ? null : banner;
        this.specialties = areas.stream().map(String::trim).distinct().toList();
    }

    public void update(String name, String title, String bio, String banner, List<String> specialties,
                       ConsultationRate newRate) {
        setDetails(name, title, bio, banner, specialties);
        this.rate = newRate;
        registerDomainEvent(new DirectoryDomainEvent("ProfessionalProfileUpdated", id, id));
    }

    public void requestVerification() {
        verificationStatus = VerificationStatus.PENDING;
        if (publicationStatus == PublicationStatus.PUBLISHED) publicationStatus = PublicationStatus.HIDDEN;
    }

    public void applyVerification(VerificationStatus status) {
        verificationStatus = status;
        if (status != VerificationStatus.APPROVED && publicationStatus == PublicationStatus.PUBLISHED)
            publicationStatus = PublicationStatus.HIDDEN;
        registerDomainEvent(new DirectoryDomainEvent(status == VerificationStatus.APPROVED
                ? "ClinicianVerified" : "VerificationRejected", id, id));
    }

    public void publish() {
        if (verificationStatus != VerificationStatus.APPROVED)
            throw new IllegalArgumentException("Only approved clinicians can publish a profile");
        publicationStatus = PublicationStatus.PUBLISHED;
        registerDomainEvent(new DirectoryDomainEvent("ProfessionalProfilePublished", id, id));
    }

    public void hide() { publicationStatus = PublicationStatus.HIDDEN; }

    public boolean isPublic() {
        return verificationStatus == VerificationStatus.APPROVED && publicationStatus == PublicationStatus.PUBLISHED;
    }

    public boolean matches(String text, String specialty, java.math.BigDecimal maxAmount, String currency) {
        String search = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return isPublic() && (displayName.toLowerCase(Locale.ROOT).contains(search)
                || specialties.stream().anyMatch(s -> s.toLowerCase(Locale.ROOT).contains(search)))
                && (specialty == null || specialties.stream().anyMatch(s -> s.equalsIgnoreCase(specialty)))
                && (maxAmount == null || rate.amount().compareTo(maxAmount) <= 0)
                && (currency == null || rate.currency().equalsIgnoreCase(currency));
    }
}
