package com.yowyob.loyaulty.program.infrastructure.redis.adapter;

import com.yowyob.loyaulty.program.domain.wallet.port.out.IdempotencyPort;
import com.yowyob.loyaulty.program.shared.util.RedisKeyBuilder;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class IdempotencyAdapter implements IdempotencyPort {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String PROCESSING = "__processing__";

    private final ReactiveRedisTemplate<String, String> redis;

    public IdempotencyAdapter(ReactiveRedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Boolean> isNew(String key) {
        return redis.hasKey(RedisKeyBuilder.idempotencyKey(key)).map(exists -> !exists);
    }

    @Override
    public Mono<Void> markProcessing(String key) {
        return redis.opsForValue()
                .set(RedisKeyBuilder.idempotencyKey(key), PROCESSING, TTL)
                .then();
    }

    @Override
    public Mono<Void> storeResponse(String key, String responseJson) {
        return redis.opsForValue()
                .set(RedisKeyBuilder.idempotencyKey(key), responseJson, TTL)
                .then();
    }

    @Override
    public Mono<String> getStoredResponse(String key) {
        return redis.opsForValue()
                .get(RedisKeyBuilder.idempotencyKey(key))
                .filter(v -> !PROCESSING.equals(v));
    }
}
