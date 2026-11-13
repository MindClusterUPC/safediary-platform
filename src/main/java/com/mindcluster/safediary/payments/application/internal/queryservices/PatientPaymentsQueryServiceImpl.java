package com.mindcluster.safediary.payments.application.internal.queryservices;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripePaymentMethodDto;
import com.mindcluster.safediary.payments.application.queryservices.PatientPaymentsQueryService;
import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.repositories.PaymentIntentRepository;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentHistoryItemResource;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PatientPaymentsQueryServiceImpl implements PatientPaymentsQueryService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StripeGatewayClient stripeGatewayClient;
    private final IamContextFacade iamContextFacade;

    public PatientPaymentsQueryServiceImpl(PaymentIntentRepository paymentIntentRepository,
                                           SubscriptionRepository subscriptionRepository,
                                           StripeGatewayClient stripeGatewayClient,
                                           @Autowired(required = false) IamContextFacade iamContextFacade) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.stripeGatewayClient = stripeGatewayClient;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryItemResource> getTransactionHistory(Long patientAccountId) {
        if (patientAccountId == null) {
            return List.of();
        }

        List<PaymentIntent> intents = paymentIntentRepository.findByPayerAccountId(patientAccountId);
        List<PaymentHistoryItemResource> result = new ArrayList<>();
        boolean hasSubscriptionIntent = false;

        for (PaymentIntent pi : intents) {
            if (!"APPROVED".equalsIgnoreCase(pi.getStatus())) {
                continue;
            }
            String currency = pi.getCurrency() != null ? pi.getCurrency().toUpperCase(Locale.ROOT) : "USD";
            String formattedAmount = formatNegativeAmount(pi.getAmount(), currency);

            if ("SUBSCRIPTION".equalsIgnoreCase(pi.getPurpose())) {
                hasSubscriptionIntent = true;
                String planName = pi.getAmount().compareTo(new BigDecimal("50")) > 0 ? "Astrum" : "Terra";
                result.add(new PaymentHistoryItemResource(
                        "pay_" + pi.getId(),
                        "Membresía SafeDiary " + planName,
                        "Suscripción " + ("Astrum".equals(planName) ? "anual • Plan Astrum" : "mensual • Plan Terra"),
                        pi.getAmount(),
                        formattedAmount,
                        currency,
                        "Recibo #SD-" + String.format("%05d", pi.getId()) + " • Pagado ✓",
                        "SUBSCRIPTION",
                        null,
                        pi.getCreatedAt() != null ? pi.getCreatedAt() : Instant.now()
                ));
            } else {
                boolean skippedOnce = "SKIPPED_ONCE".equalsIgnoreCase(pi.getProviderReference());
                String badge = skippedOnce ? "Pago omitido (Demo)" : "Verificado ✓";
                String description = skippedOnce
                        ? "Pago omitido solo por esta vez • Cita #" + pi.getSubjectId()
                        : "Sesión terapéutica • Cita #" + pi.getSubjectId();
                result.add(new PaymentHistoryItemResource(
                        "pay_" + pi.getId(),
                        "Sesión Terapéutica Profesional",
                        description,
                        pi.getAmount(),
                        formattedAmount,
                        currency,
                        "Factura #SD-" + String.format("%05d", pi.getId()) + " • " + badge,
                        "CONSULTATION",
                        "ST",
                        pi.getCreatedAt() != null ? pi.getCreatedAt() : Instant.now()
                ));
            }
        }

        // Fallback: If patient has an active paid subscription without a recorded PaymentIntent
        if (!hasSubscriptionIntent) {
            Optional<Subscription> subOpt = subscriptionRepository.findByPatientAccountId(patientAccountId);
            if (subOpt.isPresent() && subOpt.get().getPlan().isPaid() && subOpt.get().isActive()) {
                Subscription sub = subOpt.get();
                SubscriptionPlan plan = sub.getPlan();
                result.add(new PaymentHistoryItemResource(
                        "pay_sub_" + sub.getId(),
                        "Membresía SafeDiary " + plan.getDisplayName(),
                        "Suscripción " + (plan == SubscriptionPlan.ASTRUM ? "anual • Plan Astrum" : "mensual • Plan Terra"),
                        plan.getPrice(),
                        formatNegativeAmount(plan.getPrice(), plan.getCurrency()),
                        plan.getCurrency(),
                        "Recibo #SD-" + String.format("%05d", sub.getId()) + " • Pagado ✓",
                        "SUBSCRIPTION",
                        null,
                        sub.getActiveUntil() != null ? sub.getActiveUntil().minusSeconds(86400 * 30) : Instant.now()
                ));
            }
        }

        result.sort(Comparator.comparing(PaymentHistoryItemResource::createdAt).reversed());
        return result;
    }

    private static String formatNegativeAmount(BigDecimal amount, String currency) {
        String prefix = "PEN".equalsIgnoreCase(currency) ? "-S/ " : "-$";
        return prefix + amount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResource> getSavedPaymentMethods(Long patientAccountId) {
        if (patientAccountId == null) {
            return List.of();
        }

        Optional<Subscription> subOpt = subscriptionRepository.findByPatientAccountId(patientAccountId);
        if (subOpt.isPresent() && subOpt.get().getStripeCustomerId() != null && !subOpt.get().getStripeCustomerId().isBlank()) {
            List<StripePaymentMethodDto> remoteMethods = stripeGatewayClient.listCustomerPaymentMethods(subOpt.get().getStripeCustomerId());
            if (!remoteMethods.isEmpty()) {
                return remoteMethods.stream()
                        .map(pm -> new PaymentMethodResource(
                                pm.id(),
                                pm.brand(),
                                pm.last4(),
                                pm.expiry(),
                                pm.cardholder(),
                                pm.isDefault(),
                                pm.note()
                        ))
                        .toList();
            }
        }

        // If patient has a paid subscription or existing approved charges, provide standard active test wallet card
        boolean hasPaidActivity = (subOpt.isPresent() && subOpt.get().getPlan().isPaid() && subOpt.get().isActive())
                || !paymentIntentRepository.findByPayerAccountId(patientAccountId).isEmpty();

        if (hasPaidActivity) {
            String cardholder = "Elena Vance";
            if (iamContextFacade != null) {
                var accOpt = iamContextFacade.fetchAccount(patientAccountId);
                if (accOpt.isPresent() && accOpt.get().email() != null) {
                    cardholder = accOpt.get().email().split("@")[0];
                }
            }

            return List.of(new PaymentMethodResource(
                    "pm_default",
                    "VISA",
                    "4242",
                    "12/28",
                    cardholder,
                    true,
                    "Billetera SafeDiary Activa"
            ));
        }

        return List.of();
    }
}
