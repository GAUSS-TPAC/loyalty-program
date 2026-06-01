package com.yowyob.loyaulty.program.infrastructure.kafka.producer;

import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class WalletEventProducer implements WalletEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(WalletEventProducer.class);

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    public WalletEventProducer(ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publish(String topic, Object event) {
        return kafkaTemplate.send(topic, event)
                .doOnSuccess(result -> log.debug("Published {} to {}", event.getClass().getSimpleName(), topic))
                .doOnError(e -> log.error("Failed to publish to {}: {}", topic, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }

    @Override
    public Mono<Void> publishAll(String topic, List<?> events) {
        if (events == null || events.isEmpty()) return Mono.empty();
        return Mono.when(events.stream().map(event -> publish(topic, event)).toList());
    }
}
