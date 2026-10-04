package com.mindcluster.safediary.assistantai.application.internal.eventhandlers;

import com.mindcluster.safediary.assistantai.domain.model.events.CrisisProtocolActivatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to the crisis protocol activation.
 * <p>
 * Extension point for the push notification service and the IAM security audit
 * (Event Storming, flow 2). It never logs message content.
 * </p>
 */
@Component
public class CrisisProtocolActivatedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrisisProtocolActivatedEventHandler.class);

    @EventListener
    public void on(CrisisProtocolActivatedEvent event) {
        LOGGER.warn("Crisis protocol activated for session {} (account {})", event.sessionId(), event.accountId());
    }
}
