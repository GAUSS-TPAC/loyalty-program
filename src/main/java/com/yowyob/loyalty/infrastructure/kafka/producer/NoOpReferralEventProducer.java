package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.referral.event.ReferralDomainEvent;
import com.yowyob.loyalty.domain.referral.port.out.ReferralEventPublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("test")
public class NoOpReferralEventProducer implements ReferralEventPublisherPort {
    @Override
    public Mono<Void> publish(ReferralDomainEvent event) {
        return Mono.empty();
    }
}
