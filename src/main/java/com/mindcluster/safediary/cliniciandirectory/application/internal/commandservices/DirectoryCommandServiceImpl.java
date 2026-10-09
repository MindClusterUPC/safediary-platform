package com.mindcluster.safediary.cliniciandirectory.application.internal.commandservices;

import com.mindcluster.safediary.cliniciandirectory.application.commandservices.DirectoryCommandService;
import com.mindcluster.safediary.cliniciandirectory.application.internal.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.commands.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.events.DirectoryDomainEvent;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.*;
import com.mindcluster.safediary.cliniciandirectory.domain.services.ReputationCalculator;
import com.mindcluster.safediary.shared.application.result.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import java.util.function.Supplier;
import java.util.Objects;
import static com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryPolicy.*;
import static com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.DirectoryRole.*;

@Service @RequiredArgsConstructor @Transactional
public class DirectoryCommandServiceImpl implements DirectoryCommandService {
    private final ClinicianProfileRepository profiles;
    private final VerificationRequestRepository verifications;
    private final ReviewRepository reviews;
    private final ReviewReportRepository reports;
    private final ReviewHelpfulVoteRepository votes;
    private final CompletedSessionRepository sessions;
    private final RatingSummaryRepository ratings;
    private final TrustScoreRepository trustScores;
    private final ApplicationEventPublisher events;

    private <T> Result<T, ApplicationError> attempt(Supplier<T> action) {
        try { return Result.success(action.get()); }
        catch (DirectoryException ex) { return Result.failure(ex.getError()); }
        catch (IllegalArgumentException ex) { return Result.failure(ApplicationError.validationError("clinician-directory", ex.getMessage())); }
    }
    private ClinicianProfile lock(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Clinician ID must be positive");
        return profiles.findByIdForUpdate(id).orElseThrow(() -> missing("Clinician", id));
    }
    private Review lockedReview(Long id) {
        var clinicianId = reviews.findClinicianIdById(id).orElseThrow(() -> missing("Review", id));
        lock(clinicianId);
        return reviews.findById(id).orElseThrow(() -> missing("Review", id));
    }
    private void published(Review review) {
        if (review.getStatus() != ReviewStatus.PUBLISHED) throw conflict("Review is no longer published");
    }
    private void recalculate(Long clinicianId) {
        var rating = ReputationCalculator.rating(clinicianId, reviews.findByClinicianId(clinicianId));
        ratings.save(rating);
        trustScores.save(ReputationCalculator.trust(rating, sessions.findByClinicianId(clinicianId)));
        events.publishEvent(new DirectoryDomainEvent("RatingRecalculated", clinicianId, clinicianId));
        events.publishEvent(new DirectoryDomainEvent("TrustScoreRecalculated", clinicianId, clinicianId));
    }

    public Result<ClinicianProfile, ApplicationError> handle(CreateClinicianProfileCommand c) {
        return attempt(() -> {
            role(c.actor(), PSYCHOLOGIST);
            if (profiles.findByAccountId(c.actor().accountId()).isPresent()) throw conflict("Account already has a clinician profile");
            var rate = new ConsultationRate(c.amount(), c.currency(), c.durationMinutes(), 1);
            return profiles.save(new ClinicianProfile(null, c.actor().accountId(), c.displayName(), c.professionalTitle(),
                    c.bio(), c.bannerRef(), c.specialties(), rate, VerificationStatus.PENDING, PublicationStatus.DRAFT));
        });
    }
    public Result<VerificationRequest, ApplicationError> handle(RequestVerificationCommand c) {
        return attempt(() -> {
            var profile = lock(c.clinicianId()); owner(c.actor(), profile);
            var latest = verifications.findLatestByClinicianId(c.clinicianId());
            if (latest.isPresent() && latest.get().getStatus() == VerificationStatus.PENDING)
                throw conflict("A verification request is already pending");
            var request = VerificationRequest.request(c.clinicianId(), Objects.requireNonNull(c.credential()));
            profile.requestVerification(); profiles.save(profile);
            return verifications.save(request);
        });
    }
    public Result<VerificationRequest, ApplicationError> handle(ReviewCredentialsCommand c) {
        return attempt(() -> {
            role(c.actor(), ADMIN);
            var clinicianId = verifications.findClinicianIdById(c.requestId()).orElseThrow(() -> missing("Verification", c.requestId()));
            var profile = lock(clinicianId);
            var request = verifications.findById(c.requestId()).orElseThrow();
            if (profile.getAccountId().equals(c.actor().accountId())) forbidden();
            if (request.getStatus() != VerificationStatus.PENDING) throw conflict("Verification has already been reviewed");
            request.decide(c.decision(), c.actor().accountId(), c.reason());
            profile.applyVerification(c.decision());
            var saved = verifications.save(request); profiles.save(profile); return saved;
        });
    }
    public Result<ClinicianProfile, ApplicationError> handle(PublishProfileCommand c) {
        return attempt(() -> { var p = lock(c.clinicianId()); owner(c.actor(), p); p.publish(); return profiles.save(p); });
    }
    public Result<ClinicianProfile, ApplicationError> handle(HideProfileCommand c) {
        return attempt(() -> { var p = lock(c.clinicianId()); owner(c.actor(), p); p.hide(); return profiles.save(p); });
    }
    public Result<ClinicianProfile, ApplicationError> handle(UpdateBannerAndRatesCommand c) {
        return attempt(() -> {
            var p = lock(c.clinicianId()); owner(c.actor(), p);
            var candidate = new ConsultationRate(c.amount(), c.currency(), c.durationMinutes(), p.getRate().version());
            boolean rateChanged = p.getRate().amount().compareTo(candidate.amount()) != 0
                    || !p.getRate().currency().equals(candidate.currency()) || p.getRate().durationMinutes() != candidate.durationMinutes();
            var rate = rateChanged ? new ConsultationRate(c.amount(), c.currency(), c.durationMinutes(), p.getRate().version() + 1) : p.getRate();
            p.update(c.displayName(), c.professionalTitle(), c.bio(), c.bannerRef(), c.specialties(), rate);
            return profiles.save(p);
        });
    }
    public Result<Review, ApplicationError> handle(PublishReviewCommand c) {
        return attempt(() -> {
            role(c.actor(), PATIENT);
            var profile = lock(c.clinicianId());
            if (!profile.isPublic()) throw conflict("Clinician is not available in the directory");
            if (profile.getAccountId().equals(c.actor().accountId())) forbidden();
            var session = sessions.findByAppointmentId(c.appointmentId())
                    .orElseThrow(() -> conflict("Review requires a completed Care Scheduling session"));
            if (!session.clinicianId().equals(c.clinicianId()) || !session.patientAccountId().equals(c.actor().accountId())) forbidden();
            if (reviews.findByAppointmentId(c.appointmentId()).isPresent()) throw conflict("Appointment already has a review");
            var saved = reviews.save(Review.publish(c.clinicianId(), c.appointmentId(), c.actor().accountId(), c.rating(), c.text()));
            recalculate(c.clinicianId()); return saved;
        });
    }
    public Result<Review, ApplicationError> handle(DeleteOwnReviewCommand c) {
        return attempt(() -> {
            role(c.actor(), PATIENT);
            var review = lockedReview(c.reviewId());
            if (!review.getPatientAccountId().equals(c.actor().accountId())) forbidden();
            if (!c.confirmed()) throw new IllegalArgumentException("Explicit confirmation is required to delete a review");
            if (review.getStatus() != ReviewStatus.PUBLISHED) return review;
            review.withdraw(true); var saved = reviews.save(review);
            votes.deactivateByReviewId(review.getId()); recalculate(review.getClinicianId()); return saved;
        });
    }
    public Result<ReviewHelpfulVote, ApplicationError> handle(ToggleReviewHelpfulVoteCommand c) {
        return attempt(() -> {
            role(c.actor(), PATIENT, PSYCHOLOGIST, ADMIN, MODERATOR);
            var review = lockedReview(c.reviewId()); published(review);
            if (review.getPatientAccountId().equals(c.actor().accountId())) forbidden();
            var vote = votes.findByReviewIdAndAccountId(c.reviewId(), c.actor().accountId())
                    .orElse(new ReviewHelpfulVote(null, c.reviewId(), c.actor().accountId(), false));
            var saved = votes.save(vote.withActive(c.helpful()));
            events.publishEvent(new DirectoryDomainEvent("ReviewHelpfulVoteChanged", review.getClinicianId(), c.reviewId()));
            return saved;
        });
    }
    public Result<ReviewReport, ApplicationError> handle(ReportReviewCommand c) {
        return attempt(() -> {
            role(c.actor(), PATIENT, PSYCHOLOGIST, ADMIN, MODERATOR);
            var review = lockedReview(c.reviewId()); published(review);
            if (reports.existsPending(c.reviewId(), c.actor().accountId())) throw conflict("A report is already pending for this review");
            return reports.save(ReviewReport.report(review.getClinicianId(), c.reviewId(), c.actor().accountId(), c.reason(), c.comment()));
        });
    }
    public Result<ReviewReport, ApplicationError> handle(ResolveReviewReportCommand c) {
        return attempt(() -> {
            role(c.actor(), ADMIN, MODERATOR);
            var reviewId = reports.findReviewIdById(c.reportId()).orElseThrow(() -> missing("ReviewReport", c.reportId()));
            var review = lockedReview(reviewId);
            var report = reports.findById(c.reportId()).orElseThrow();
            if (report.getReporterAccountId().equals(c.actor().accountId())
                    || review.getPatientAccountId().equals(c.actor().accountId())
                    || profiles.findById(review.getClinicianId()).orElseThrow().getAccountId().equals(c.actor().accountId())) forbidden();
            if (report.getStatus() != ReportStatus.PENDING) throw conflict("Report is already resolved");
            report.resolve(review.getClinicianId(), c.actor().accountId(), c.removeReview(), c.resolution());
            var saved = reports.save(report);
            if (c.removeReview() && review.getStatus() == ReviewStatus.PUBLISHED) {
                review.withdraw(false); reviews.save(review); votes.deactivateByReviewId(review.getId()); recalculate(review.getClinicianId());
            }
            return saved;
        });
    }
    public Result<CompletedSession, ApplicationError> handle(RecordCompletedSessionCommand c) {
        return attempt(() -> {
            var session = new CompletedSession(null, c.appointmentId(), c.clinicianId(), c.patientAccountId(), c.durationMinutes(), c.completedAt());
            lock(c.clinicianId());
            var existing = sessions.findByAppointmentId(c.appointmentId());
            if (existing.isPresent()) {
                var old = existing.get();
                if (!old.clinicianId().equals(c.clinicianId()) || !old.patientAccountId().equals(c.patientAccountId())
                        || old.durationMinutes() != c.durationMinutes() || !old.completedAt().equals(session.completedAt()))
                    throw conflict("Completed session replay does not match the original event");
                return old;
            }
            var saved = sessions.save(session); recalculate(c.clinicianId()); return saved;
        });
    }
}
