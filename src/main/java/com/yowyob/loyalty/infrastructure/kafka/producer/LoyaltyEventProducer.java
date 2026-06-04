package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.port.out.LoyaltyEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("!test")
public class LoyaltyEventProducer implements LoyaltyEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyEventProducer.class);

    private final ReactiveKafkaProducerTemplate<String, EventProcessingResult> kafkaTemplate;
    private final String topicPrefix;

    public LoyaltyEventProducer(
            ReactiveKafkaProducerTemplate<String, EventProcessingResult> loyaltyEventKafkaTemplate,
            @Value("${spring.kafka.topics.loyalty-events:loyalty.events}") String topicPrefix
    ) {
        this.kafkaTemplate = loyaltyEventKafkaTemplate;
        this.topicPrefix = topicPrefix;
    }

    @Override
    public void publishProcessedEvent(EventProcessingResult result) {
        String topic = topicPrefix + "." + result.tenantId().value();
        kafkaTemplate.send(topic, result.eventId(), result)
                .doOnError(e -> log.error("Failed to publish loyalty event to {}: {}", topic, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}
