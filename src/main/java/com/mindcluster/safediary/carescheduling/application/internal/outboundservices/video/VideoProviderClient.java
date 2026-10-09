package com.mindcluster.safediary.carescheduling.application.internal.outboundservices.video;

public interface VideoProviderClient {
    /** Creates a private room for a confirmed appointment and returns its join URL. */
    String createRoom(Long appointmentId);
}
