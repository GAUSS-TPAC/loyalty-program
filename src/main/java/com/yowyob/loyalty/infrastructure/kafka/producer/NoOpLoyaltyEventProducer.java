package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.port.out.LoyaltyEventPublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test | no-kafka")
public class NoOpLoyaltyEventProducer implements LoyaltyEventPublisherPort {

    @Override
    public void publishProcessedEvent(EventProcessingResult result) {
        // no-op: unit/integration tests, or Kafka disabled via the no-kafka profile
    }
}
