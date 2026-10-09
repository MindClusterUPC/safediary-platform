package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.transform;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.*;
import com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources.*;

public final class DirectoryResourceAssembler {
    private DirectoryResourceAssembler() {}
    public static ProfessionalProfileResource profile(ClinicianProfile p) {
        var rate = p.getRate();
        return new ProfessionalProfileResource(p.getId(), p.getDisplayName(), p.getProfessionalTitle(), p.getBio(),
                p.getBannerRef(), p.getSpecialties(), new ConsultationRateResource(rate.amount(), rate.currency(),
                rate.durationMinutes(), rate.version()), p.getVerificationStatus(), p.getPublicationStatus());
    }
    public static VerificationRequestResource verification(VerificationRequest v) {
        var c = v.getCredential();
        return new VerificationRequestResource(v.getId(), v.getClinicianId(), c.licenseNumber(), c.specialty(), c.documentRef(),
                v.getStatus(), v.getSubmittedAt(), v.getReviewedByAccountId(), v.getReviewedAt(), v.getRejectionReason());
    }
    public static ReviewResource review(Review r, long helpfulCount) {
        return new ReviewResource(r.getId(), r.getClinicianId(), r.getRating(), r.getText(), r.getPublishedAt(), helpfulCount);
    }
    public static ReviewReportResource report(ReviewReport r) {
        return new ReviewReportResource(r.getId(), r.getReviewId(), r.getReason(), r.getComment(), r.getStatus(),
                r.getReportedAt(), r.getResolvedByAccountId(), r.getResolvedAt(), r.getResolution());
    }
    public static RatingSummaryResource rating(RatingSummary r) { return new RatingSummaryResource(r.clinicianId(), r.average(), r.reviewCount()); }
    public static TrustScoreResource trust(TrustScore t) {
        return new TrustScoreResource(t.clinicianId(), t.score(), t.sufficientData(), t.ratingFactor(), t.completedSessions(),
                t.completedMinutes(), t.reviewCount(), 3, "v1: average published rating / 5 * 100; minimum 3 reviews. Session volume and helpful votes do not increase the score.");
    }
}
