package com.mindcluster.safediary.carescheduling.application.internal.eventhandlers;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.domain.model.commands.ExpireSlotHoldsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

/** Expires one-hour holds that were not paid in time and releases their slot for other patients. */
@Component
@ConditionalOnProperty(name = "care-scheduling.hold-expiration.scheduler.enabled", havingValue = "true")
public class SlotHoldExpirationEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlotHoldExpirationEventHandler.class);

    private final CareSchedulingCommandService commandService;

    public SlotHoldExpirationEventHandler(CareSchedulingCommandService commandService) {
        this.commandService = commandService;
    }

    @Scheduled(fixedDelayString = "${care-scheduling.hold-expiration.fixed-delay-ms:60000}")
    public void expireHolds() {
        int expired = commandService.handle(new ExpireSlotHoldsCommand(Instant.now()));
        if (expired > 0) LOGGER.info("Released {} expired slot holds", expired);
    }
}
