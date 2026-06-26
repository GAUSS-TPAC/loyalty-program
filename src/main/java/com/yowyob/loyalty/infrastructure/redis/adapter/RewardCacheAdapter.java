package com.yowyob.loyalty.infrastructure.redis.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.port.out.RewardCachePort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RewardCacheAdapter implements RewardCachePort {

    private static final Logger log = LoggerFactory.getLogger(RewardCacheAdapter.class);
    private static final Duration CATALOG_TTL = Duration.ofMinutes(10);
    private static final Duration REWARD_TTL = Duration.ofMinutes(30);

    private final ReactiveRedisTemplate<String, String> redis;
    private final ObjectMapper objectMapper;

    public RewardCacheAdapter(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis,
            ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<List<Reward>> getCachedCatalog(TenantId tenantId) {
        return redis.opsForValue().get(RedisKeyBuilder.rewardCatalogKey(tenantId))
                .flatMap(json -> Mono.fromCallable(() -> {
                    TypeReference<List<Reward>> type = new TypeReference<>() {};
                    return objectMapper.readValue(json, type);
                }))
                .onErrorResume(e -> Mono.empty());
    }

    @Override
    public Mono<Void> cacheCatalog(TenantId tenantId, List<Reward> rewards, Duration ttl) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(rewards))
                .flatMap(json -> redis.opsForValue().set(RedisKeyBuilder.rewardCatalogKey(tenantId), json, ttl))
                .onErrorResume(e -> {
                    log.warn("Failed to cache reward catalog: {}", e.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public Mono<Void> evictCatalog(TenantId tenantId) {
        return redis.delete(RedisKeyBuilder.rewardCatalogKey(tenantId))
                .onErrorResume(e -> Mono.empty())
                .then();
    }

    @Override
    public Mono<Optional<Reward>> getCachedReward(TenantId tenantId, UUID rewardId) {
        return redis.opsForValue().get(RedisKeyBuilder.rewardKey(tenantId, rewardId))
                .flatMap(json -> Mono.fromCallable(() -> Optional.of(objectMapper.readValue(json, Reward.class))))
                .defaultIfEmpty(Optional.empty())
                .onErrorResume(e -> Mono.just(Optional.empty()));
    }

    @Override
    public Mono<Void> cacheReward(TenantId tenantId, Reward reward, Duration ttl) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(reward))
                .flatMap(json -> redis.opsForValue().set(RedisKeyBuilder.rewardKey(tenantId, reward.id()), json, ttl))
                .onErrorResume(e -> Mono.empty())
                .then();
    }

    @Override
    public Mono<Void> evictReward(TenantId tenantId, UUID rewardId) {
        return redis.delete(RedisKeyBuilder.rewardKey(tenantId, rewardId))
                .onErrorResume(e -> Mono.empty())
                .then();
    }
}
