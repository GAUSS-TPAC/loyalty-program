package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.port.out.LoyaltyEventPublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoOpLoyaltyEventProducer implements LoyaltyEventPublisherPort {

    @Override
    public void publishProcessedEvent(EventProcessingResult result) {
        // no-op for unit/integration tests without Kafka
    }
}
