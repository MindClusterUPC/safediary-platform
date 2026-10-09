package com.mindcluster.safediary.cliniciandirectory.application.internal.queryservices;

import com.mindcluster.safediary.cliniciandirectory.application.queryservices.DirectoryQueryService;
import com.mindcluster.safediary.cliniciandirectory.domain.model.queries.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.*;
import com.mindcluster.safediary.cliniciandirectory.domain.services.ReputationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import static com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryPolicy.*;
import static com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.DirectoryRole.*;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DirectoryQueryServiceImpl implements DirectoryQueryService {
    private final ClinicianProfileRepository profiles;
    private final VerificationRequestRepository verifications;
    private final ReviewRepository reviews;
    private final ReviewReportRepository reports;
    private final ReviewHelpfulVoteRepository votes;
    private final RatingSummaryRepository ratings;
    private final TrustScoreRepository trustScores;

    private ClinicianProfile publicProfile(Long id) {
        return profiles.findById(id).filter(ClinicianProfile::isPublic).orElseThrow(() -> missing("Clinician", id));
    }
    public List<ClinicianProfile> handle(SearchCliniciansQuery q) {
        if (q.page() < 0 || q.size() < 1 || q.size() > 100 || (q.maxAmount() != null && q.maxAmount().signum() < 0))
            throw new IllegalArgumentException("Page must be non-negative, size 1-100 and maxAmount non-negative");
        if (q.maxAmount() != null && q.currency() == null)
            throw new IllegalArgumentException("Currency is required when filtering by price");
        if (q.currency() != null) java.util.Currency.getInstance(q.currency().toUpperCase(java.util.Locale.ROOT));
        return profiles.findPublicProfiles().stream().filter(p -> p.matches(q.text(), q.specialty(), q.maxAmount(), q.currency()))
                .sorted(Comparator.comparing(ClinicianProfile::getId)).skip((long)q.page() * q.size()).limit(q.size()).toList();
    }
    public Optional<ClinicianProfile> handle(GetClinicianProfileQuery q) { return profiles.findById(q.clinicianId()).filter(ClinicianProfile::isPublic); }
    public Optional<ClinicianProfile> handle(GetOwnClinicianProfileQuery q) { role(q.actor(), PSYCHOLOGIST); return profiles.findByAccountId(q.actor().accountId()); }
    public Optional<VerificationRequest> handle(GetVerificationStatusQuery q) {
        var p = profiles.findById(q.clinicianId()).orElseThrow(() -> missing("Clinician", q.clinicianId()));
        if (q.actor() != null && q.actor().role() == ADMIN) role(q.actor(), ADMIN); else owner(q.actor(), p);
        return verifications.findLatestByClinicianId(q.clinicianId());
    }
    public List<VerificationRequest> handle(GetPendingVerificationsQuery q) { role(q.actor(), ADMIN); return verifications.findPending(); }
    public RatingSummary handle(GetRatingSummaryQuery q) {
        publicProfile(q.clinicianId());
        return ratings.findByClinicianId(q.clinicianId()).orElse(new RatingSummary(q.clinicianId(), null, 0));
    }
    public TrustScore handle(GetTrustScoreBreakdownQuery q) {
        var p = profiles.findById(q.clinicianId()).orElseThrow(() -> missing("Clinician", q.clinicianId())); owner(q.actor(), p);
        return trustScores.findByClinicianId(q.clinicianId())
                .orElse(ReputationCalculator.trust(new RatingSummary(q.clinicianId(), null, 0), List.of()));
    }
    public List<ReviewReport> handle(GetReviewReportsQuery q) { role(q.actor(), ADMIN, MODERATOR); return reports.findByStatus(q.status()); }
    public List<Review> handle(GetClinicianReviewsQuery q) {
        publicProfile(q.clinicianId());
        return reviews.findByClinicianId(q.clinicianId()).stream().filter(r -> r.getStatus() == ReviewStatus.PUBLISHED).toList();
    }
    public long helpfulCount(Long id) { return votes.countActive(id); }
}
