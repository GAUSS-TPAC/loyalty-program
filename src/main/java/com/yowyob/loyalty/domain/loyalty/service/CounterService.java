package com.yowyob.loyalty.domain.loyalty.service;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;

import java.util.Map;

public class CounterService {

    public Counter processIncrement(Counter counter, IncomingEvent event, long delta, String windowType) {
        if (counter == null) {
            return new Counter(
                    java.util.UUID.randomUUID(),
                    event.tenantId(),
                    event.memberId(),
                    "TBD", // Requires key from context
                    delta,
                    windowType,
                    event.occurredAt(),
                    event.occurredAt()
            );
        }

        if (counter.isExpiredWindow(event.occurredAt())) {
            Counter reset = counter.reset();
            return new Counter(reset.id(), reset.tenantId(), reset.memberId(), reset.counterKey(),
                    delta, windowType, event.occurredAt(), event.occurredAt());
        }

        return counter.increment(delta);
    }

    public Counter processReset(Counter counter, IncomingEvent event) {
        if (counter == null) return null;
        return counter.reset();
    }
}
