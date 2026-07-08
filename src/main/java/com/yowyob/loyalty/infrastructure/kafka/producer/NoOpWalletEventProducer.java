package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.wallet.event.WalletDomainEvent;
import com.yowyob.loyalty.domain.wallet.port.out.WalletEventPublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("test | no-kafka")
public class NoOpWalletEventProducer implements WalletEventPublisherPort {

    @Override
    public Mono<Void> publish(WalletDomainEvent event) {
        return Mono.empty();
    }
}
