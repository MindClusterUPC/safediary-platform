package com.mindcluster.safediary.carescheduling.domain.model.entities;

import java.time.Instant;

/** Private coordination chat message; it never grants access to the patient diary. */
public record CoordinationMessage(Long id, Long contactRequestId, Long senderAccountId, String body, Instant sentAt) {
    public static final int MAX_LENGTH = 500;
    public CoordinationMessage {
        if (senderAccountId == null || senderAccountId <= 0 || sentAt == null)
            throw new IllegalArgumentException("A message requires a sender and a sending time");
        if (body == null || body.isBlank() || body.trim().length() > MAX_LENGTH)
            throw new IllegalArgumentException("Message must contain between 1 and 500 characters");
        body = body.trim();
    }
}
