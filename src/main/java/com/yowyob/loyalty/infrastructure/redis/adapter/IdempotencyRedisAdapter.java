package com.yowyob.loyalty.infrastructure.redis.adapter;

import com.yowyob.loyalty.domain.wallet.port.out.IdempotencyPort;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class IdempotencyRedisAdapter implements IdempotencyPort {

    private final ReactiveRedisTemplate<String, String> redis;

    public IdempotencyRedisAdapter(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Boolean> exists(String idempotencyKey, String tenantId) {
        return redis.hasKey(key(idempotencyKey, tenantId));
    }

    @Override
    public Mono<Boolean> registerIfAbsent(String idempotencyKey, String tenantId, Duration ttl, String resultPayload) {
        return redis.opsForValue()
                .setIfAbsent(key(idempotencyKey, tenantId), resultPayload, ttl);
    }

    @Override
    public Mono<String> getResult(String idempotencyKey, String tenantId) {
        return redis.opsForValue().get(key(idempotencyKey, tenantId));
    }

    private static String key(String idempotencyKey, String tenantId) {
        return RedisKeyBuilder.idempotencyKey(tenantId + ":" + idempotencyKey);
    }
}
