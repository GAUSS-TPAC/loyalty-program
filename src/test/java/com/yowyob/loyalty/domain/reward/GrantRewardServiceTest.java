package com.yowyob.loyalty.domain.reward;

import com.yowyob.loyalty.domain.reward.event.RewardDomainEvent;
import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.reward.model.*;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.reward.port.out.RewardRepository;
import com.yowyob.loyalty.domain.reward.service.GrantRewardService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrantRewardServiceTest {

    private final Map<UUID, Reward> rewards = new ConcurrentHashMap<>();
    private final Map<UUID, RewardGrant> grants = new ConcurrentHashMap<>();
    private GrantRewardService service;
    private final TenantId tenantId = new TenantId(UUID.randomUUID());
    private final UserId memberId = new UserId(UUID.randomUUID());

    @BeforeEach
    void setup() {
        RewardRepository rewardRepo = new RewardRepository() {
            public Mono<Reward> save(Reward r) { rewards.put(r.id(), r); return Mono.just(r); }
            public Mono<Reward> findById(UUID id) { return Mono.justOrEmpty(rewards.get(id)); }
            public Mono<Reward> findByIdAndTenant(UUID id, TenantId t) { return Mono.justOrEmpty(rewards.get(id)); }
            public Flux<Reward> findByTenant(TenantId t, boolean activeOnly, int page, int size) { return Flux.fromIterable(rewards.values()); }
            public Mono<Boolean> existsByIdAndTenant(UUID id, TenantId t) { return Mono.just(rewards.containsKey(id)); }
        };
        RewardGrantRepository grantRepo = new RewardGrantRepository() {
            public Mono<RewardGrant> save(RewardGrant g) { grants.put(g.id(), g); return Mono.just(g); }
            public Mono<RewardGrant> findById(UUID id) { return Mono.justOrEmpty(grants.get(id)); }
            public Mono<RewardGrant> findByIdAndTenant(UUID id, TenantId t) { return Mono.justOrEmpty(grants.get(id)); }
            public Mono<RewardGrant> findByIdempotencyKey(String key) { return Mono.empty(); }
            public Flux<RewardGrant> findActiveByMember(UserId m, TenantId t) { return Flux.fromIterable(grants.values()); }
            public Flux<RewardGrant> findAllByMember(UserId m, TenantId t, int page, int size) { return Flux.fromIterable(grants.values()); }
            public Flux<RewardGrant> findExpiredActive(Instant before) { return Flux.empty(); }
        };
        RewardEventPublisherPort publisher = event -> Mono.empty();
        service = new GrantRewardService(rewardRepo, grantRepo, publisher);
    }

    private Reward activeReward(Integer stock) {
        Reward reward = Reward.create(UUID.randomUUID(), tenantId, "Reward", "d", RewardType.FREE_PRODUCT,
                RewardValue.product("SKU"), 0, stock, null, null, 0, null, null).activate();
        rewards.put(reward.id(), reward);
        return reward;
    }

    @Test
    void grantReward_happyPath_activatesGrantAndDecrementsStock() {
        Reward reward = activeReward(5);

        StepVerifier.create(service.grantReward(tenantId, memberId, reward.id(),
                        GrantSource.RULE_ENGINE, null, "evt-1", "idem-1"))
                .assertNext(grant -> assertEquals(GrantStatus.ACTIVE, grant.status()))
                .verifyComplete();

        assertEquals(4, rewards.get(reward.id()).stockRemaining(), "stock must be decremented by the grant");
    }

    @Test
    void grantReward_unknownReward_yieldsEmptyMonoNotAnException() {
        // Documented quirk: no switchIfEmpty here, so an unknown reward id produces an empty
        // Mono rather than a RewardNotFoundException -- callers must not assume an error signal.
        StepVerifier.create(service.grantReward(tenantId, memberId, UUID.randomUUID(),
                        GrantSource.RULE_ENGINE, null, "evt-1", "idem-1"))
                .verifyComplete();
    }

    @Test
    void grantReward_unavailableReward_throws() {
        Reward draft = Reward.create(UUID.randomUUID(), tenantId, "Reward", "d", RewardType.FREE_PRODUCT,
                RewardValue.product("SKU"), 0, null, null, null, 0, null, null); // stays DRAFT
        rewards.put(draft.id(), draft);

        StepVerifier.create(service.grantReward(tenantId, memberId, draft.id(),
                        GrantSource.RULE_ENGINE, null, "evt-1", "idem-1"))
                .expectError(RewardDomainException.class)
                .verify();
    }

    @Test
    void grantReward_exhaustedStock_throws() {
        Reward reward = activeReward(1);
        reward.decrementStock(); // now at 0 / EXHAUSTED
        rewards.put(reward.id(), reward);

        StepVerifier.create(service.grantReward(tenantId, memberId, reward.id(),
                        GrantSource.RULE_ENGINE, null, "evt-1", "idem-1"))
                .expectError(RewardDomainException.class)
                .verify();
    }

    @Test
    void grantReward_idempotencyKeyParameter_isAcceptedButNeverActuallyChecked() {
        // Documented quirk: idempotencyKey is part of the signature but this service never
        // queries findByIdempotencyKey nor guards against duplicate calls with the same key --
        // calling twice with the identical key grants twice and double-decrements stock.
        Reward reward = activeReward(5);

        service.grantReward(tenantId, memberId, reward.id(), GrantSource.RULE_ENGINE, null, "evt-1", "same-key").block();
        service.grantReward(tenantId, memberId, reward.id(), GrantSource.RULE_ENGINE, null, "evt-1", "same-key").block();

        assertEquals(3, rewards.get(reward.id()).stockRemaining(), "no idempotency guard: both calls decremented stock");
        assertEquals(2, grants.size(), "no idempotency guard: two distinct grants were created");
    }
}
