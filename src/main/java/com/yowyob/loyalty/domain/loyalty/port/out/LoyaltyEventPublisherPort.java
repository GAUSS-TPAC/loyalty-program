package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;

public interface LoyaltyEventPublisherPort {
    void publishProcessedEvent(EventProcessingResult result);
}
