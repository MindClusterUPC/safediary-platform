package com.mindcluster.safediary.carescheduling.domain.model.commands;

import java.math.BigDecimal;

public record ConfirmAfterPaymentCommand(Long appointmentId, String paymentReference, BigDecimal amount, String currency, boolean approved) {}
