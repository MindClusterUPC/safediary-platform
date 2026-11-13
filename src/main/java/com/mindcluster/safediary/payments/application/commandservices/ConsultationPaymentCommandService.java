package com.mindcluster.safediary.payments.application.commandservices;

import com.mindcluster.safediary.payments.application.commandservices.dto.ConsultationCheckoutResultDto;
import com.mindcluster.safediary.payments.application.commandservices.dto.SkipConsultationPaymentResultDto;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConsultationCheckoutCommand;
import com.mindcluster.safediary.payments.domain.model.commands.SkipConsultationPaymentCommand;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface ConsultationPaymentCommandService {
    Result<ConsultationCheckoutResultDto, ApplicationError> handle(CreateConsultationCheckoutCommand command);

    Result<SkipConsultationPaymentResultDto, ApplicationError> handle(SkipConsultationPaymentCommand command);
}
