package com.mindcluster.safediary.shared.infrastructure.events;

import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.mindcluster.safediary.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes and clears domain events registered by aggregate roots.
 */
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishAndClear(AbstractDomainAggregateRoot<?> aggregate) {
        aggregate.domainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }

    public void publishAndClear(AuditableAbstractAggregateRoot<?> aggregate) {
        aggregate.domainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }
}
