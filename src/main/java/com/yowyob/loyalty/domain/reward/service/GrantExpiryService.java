package com.yowyob.loyalty.domain.reward.service;

import com.yowyob.loyalty.domain.reward.event.RewardGrantExpiredEvent;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GrantExpiryService {

    private final RewardGrantRepository grantRepo;
    private final RewardEventPublisherPort eventPublisher;

    public GrantExpiryService(RewardGrantRepository grantRepo, RewardEventPublisherPort eventPublisher) {
        this.grantRepo = grantRepo;
        this.eventPublisher = eventPublisher;
    }

    public Mono<Integer> expireOutdatedGrants(Instant now) {
        AtomicInteger count = new AtomicInteger(0);
        return grantRepo.findExpiredActive(now)
                .flatMap(grant -> {
                    grant.expire();
                    return grantRepo.save(grant)
                            .flatMap(saved -> eventPublisher.publish(
                                    new RewardGrantExpiredEvent(UUID.randomUUID(), Instant.now(),
                                            saved.tenantId(), saved.memberId(),
                                            saved.id(), saved.rewardId()))
                                    .thenReturn(saved));
                })
                .doOnNext(g -> count.incrementAndGet())
                .then(Mono.fromCallable(count::get));
    }
}
