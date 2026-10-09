package com.mindcluster.safediary.carescheduling.infrastructure.video;

import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.video.VideoProviderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Video provider adapter based on unguessable room names under a configurable base URL. The join URL is only
 * returned to authenticated participants inside the access window; replace it with a provider that issues
 * per-participant tokens before production.
 */
@Component
public class ConfigurableVideoProviderAdapter implements VideoProviderClient {
    private final String baseUrl;

    public ConfigurableVideoProviderAdapter(@Value("${care-scheduling.video.base-url:https://meet.jit.si/}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public String createRoom(Long appointmentId) {
        return baseUrl + "safediary-" + appointmentId + "-" + UUID.randomUUID();
    }
}
