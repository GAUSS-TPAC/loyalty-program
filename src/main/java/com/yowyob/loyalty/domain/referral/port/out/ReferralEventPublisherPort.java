package com.yowyob.loyalty.domain.referral.port.out;

import com.yowyob.loyalty.domain.referral.event.ReferralDomainEvent;
import reactor.core.publisher.Mono;

public interface ReferralEventPublisherPort {
    Mono<Void> publish(ReferralDomainEvent event);
}
