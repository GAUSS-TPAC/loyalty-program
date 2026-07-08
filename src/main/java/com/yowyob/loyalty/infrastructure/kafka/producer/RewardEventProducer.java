package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.reward.event.RewardDomainEvent;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("!test & !no-kafka")
public class RewardEventProducer implements RewardEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RewardEventProducer.class);
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    public RewardEventProducer(ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publish(RewardDomainEvent event) {
        String topic = "loyalty.reward.events." + event.tenantId().value();
        String key = event.tenantId().value().toString();

        return kafkaTemplate.send(topic, key, event)
                .doOnSuccess(r -> log.debug("Event {} published to {}", event.eventType(), topic))
                .doOnError(e -> log.error("Failed to publish {}: {}", event.eventType(), e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }
}
