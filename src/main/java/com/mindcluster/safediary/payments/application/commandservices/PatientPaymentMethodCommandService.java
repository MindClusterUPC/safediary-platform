package com.mindcluster.safediary.payments.application.commandservices;

import com.mindcluster.safediary.payments.application.commandservices.dto.PaymentMethodSetupResultDto;
import com.mindcluster.safediary.payments.domain.model.commands.CreatePaymentMethodSetupCommand;
import com.mindcluster.safediary.payments.domain.model.commands.SetDefaultPaymentMethodCommand;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodResource;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

import java.util.List;

public interface PatientPaymentMethodCommandService {

    Result<PaymentMethodSetupResultDto, ApplicationError> handle(CreatePaymentMethodSetupCommand command);

    Result<List<PaymentMethodResource>, ApplicationError> handle(SetDefaultPaymentMethodCommand command);
}
