package com.mindcluster.safediary.payments.application.queryservices;

import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentHistoryItemResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodResource;

import java.util.List;

public interface PatientPaymentsQueryService {
    List<PaymentHistoryItemResource> getTransactionHistory(Long patientAccountId);
    List<PaymentMethodResource> getSavedPaymentMethods(Long patientAccountId);
}
