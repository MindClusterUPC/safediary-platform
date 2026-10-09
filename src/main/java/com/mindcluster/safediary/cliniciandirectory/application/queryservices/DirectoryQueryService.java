package com.mindcluster.safediary.cliniciandirectory.application.queryservices;

import com.mindcluster.safediary.cliniciandirectory.domain.model.queries.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.*;
import java.util.List;
import java.util.Optional;
public interface DirectoryQueryService {
    List<ClinicianProfile> handle(SearchCliniciansQuery query);
    Optional<ClinicianProfile> handle(GetClinicianProfileQuery query);
    Optional<ClinicianProfile> handle(GetOwnClinicianProfileQuery query);
    Optional<VerificationRequest> handle(GetVerificationStatusQuery query);
    List<VerificationRequest> handle(GetPendingVerificationsQuery query);
    RatingSummary handle(GetRatingSummaryQuery query);
    TrustScore handle(GetTrustScoreBreakdownQuery query);
    List<ReviewReport> handle(GetReviewReportsQuery query);
    List<Review> handle(GetClinicianReviewsQuery query);
    long helpfulCount(Long reviewId);
}
