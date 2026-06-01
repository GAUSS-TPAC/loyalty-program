package com.yowyob.loyaulty.program.infrastructure.bonification.event;

import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationEventPort;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;
import com.yowyob.loyaulty.program.infrastructure.kafka.producer.WalletEventProducer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BonificationEventAdapter implements BonificationEventPort {

    private static final String TOPIC = "loyalty.events";

    private final WalletEventProducer eventProducer;

    public BonificationEventAdapter(WalletEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @Override
    public Mono<Void> publish(DomainEvent event) {
        return eventProducer.publish(TOPIC, event);
    }
}
