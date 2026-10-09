package com.mindcluster.safediary.cliniciandirectory.application.commandservices;

import com.mindcluster.safediary.cliniciandirectory.domain.model.commands.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.*;
import com.mindcluster.safediary.shared.application.result.*;
public interface DirectoryCommandService {
    Result<ClinicianProfile, ApplicationError> handle(CreateClinicianProfileCommand command);
    Result<VerificationRequest, ApplicationError> handle(RequestVerificationCommand command);
    Result<VerificationRequest, ApplicationError> handle(ReviewCredentialsCommand command);
    Result<ClinicianProfile, ApplicationError> handle(PublishProfileCommand command);
    Result<ClinicianProfile, ApplicationError> handle(HideProfileCommand command);
    Result<ClinicianProfile, ApplicationError> handle(UpdateBannerAndRatesCommand command);
    Result<Review, ApplicationError> handle(PublishReviewCommand command);
    Result<Review, ApplicationError> handle(DeleteOwnReviewCommand command);
    Result<ReviewHelpfulVote, ApplicationError> handle(ToggleReviewHelpfulVoteCommand command);
    Result<ReviewReport, ApplicationError> handle(ReportReviewCommand command);
    Result<ReviewReport, ApplicationError> handle(ResolveReviewReportCommand command);
    Result<CompletedSession, ApplicationError> handle(RecordCompletedSessionCommand command);
}
