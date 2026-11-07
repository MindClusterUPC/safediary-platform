package com.mindcluster.safediary.payments.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeProperties {

    private String apiKey;
    private String publicKey;
    private String webhookSecret;
    private String connectWebhookSecret;
    private String successUrl = "http://localhost:8080/api/v1/payments/subscriptions/success";
    private String cancelUrl = "http://localhost:8080/api/v1/payments/subscriptions/cancel";
    private ConnectProperties connect = new ConnectProperties();
    private MarketplaceProperties marketplace = new MarketplaceProperties();
    private SubscriptionProperties subscription = new SubscriptionProperties();

    @Getter
    @Setter
    public static class ConnectProperties {
        private String refreshUrl = "http://localhost:8080/api/v1/payments/connect/refresh";
        private String returnUrl = "http://localhost:8080/api/v1/payments/connect/return";
    }

    @Getter
    @Setter
    public static class MarketplaceProperties {
        private double feePercent = 15.0;
    }

    @Getter
    @Setter
    public static class SubscriptionProperties {
        private PlanProperties terra = new PlanProperties();
        private PlanProperties astrum = new PlanProperties();
    }

    @Getter
    @Setter
    public static class PlanProperties {
        private String priceId;
    }
}
