package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.wallet.event.WalletDomainEvent;
import com.yowyob.loyalty.domain.wallet.port.out.WalletEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class WalletEventProducer implements WalletEventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(WalletEventProducer.class);
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    public WalletEventProducer(ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publish(WalletDomainEvent event) {
        // Topic name: loyalty.wallet.events.{tenantId}
        // Key: memberId logic needed if available in event, otherwise random or walletId
        String topic = "loyalty.wallet.events.global"; // Simplifié ou dynamique selon besoin
        
        return kafkaTemplate.send(topic, event)
            .doOnSuccess(result -> log.debug("Event published: {}", event.getClass().getSimpleName()))
            .doOnError(err -> log.error("Error publishing event: {}", err.getMessage()))
            .then()
            .onErrorResume(e -> Mono.empty()); // On ne fait pas échouer l'opération principale
    }
}
