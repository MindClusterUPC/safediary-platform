package com.mindcluster.safediary.iam.application.internal.outboundservices.notifications;

import java.time.Instant;

public interface EmailNotificationClient {
    void sendPasswordResetCode(String email, String code, Instant expiresAt);
}
