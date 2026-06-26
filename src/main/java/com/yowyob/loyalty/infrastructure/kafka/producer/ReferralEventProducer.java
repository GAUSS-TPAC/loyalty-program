package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.referral.event.ReferralDomainEvent;
import com.yowyob.loyalty.domain.referral.port.out.ReferralEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("!test")
public class ReferralEventProducer implements ReferralEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(ReferralEventProducer.class);
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    public ReferralEventProducer(ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publish(ReferralDomainEvent event) {
        String topic = "loyalty.referral.events." + event.tenantId().value();
        return kafkaTemplate.send(topic, event.eventType(), event)
                .doOnSuccess(r -> log.debug("Referral event published: {}", event.eventType()))
                .doOnError(e -> log.error("Failed to publish referral event {}: {}", event.eventType(), e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }
}
