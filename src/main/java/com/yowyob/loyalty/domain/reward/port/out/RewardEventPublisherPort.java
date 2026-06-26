package com.yowyob.loyalty.domain.reward.port.out;

import com.yowyob.loyalty.domain.reward.event.RewardDomainEvent;
import reactor.core.publisher.Mono;

public interface RewardEventPublisherPort {
    Mono<Void> publish(RewardDomainEvent event);
}
