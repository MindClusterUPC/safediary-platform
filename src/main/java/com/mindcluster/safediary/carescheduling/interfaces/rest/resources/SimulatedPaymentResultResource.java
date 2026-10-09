package com.mindcluster.safediary.carescheduling.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** What Payments and Payouts would publish after the gateway confirms or rejects the charge. */
public record SimulatedPaymentResultResource(@NotBlank String paymentReference, boolean approved,
                                             @NotNull @PositiveOrZero BigDecimal amount, @NotBlank String currency) {}
