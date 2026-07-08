package com.yowyob.loyalty.infrastructure.kafka.producer;

import com.yowyob.loyalty.domain.reward.event.RewardDomainEvent;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("test | no-kafka")
public class NoOpRewardEventProducer implements RewardEventPublisherPort {

    @Override
    public Mono<Void> publish(RewardDomainEvent event) {
        return Mono.empty();
    }
}
