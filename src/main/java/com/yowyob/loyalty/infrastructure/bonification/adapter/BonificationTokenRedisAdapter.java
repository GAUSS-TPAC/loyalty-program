package com.yowyob.loyalty.infrastructure.bonification.adapter;

import com.yowyob.loyalty.domain.bonification.port.out.BonificationTokenCachePort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class BonificationTokenRedisAdapter implements BonificationTokenCachePort {

    private final ReactiveRedisTemplate<String, String> redis;

    public BonificationTokenRedisAdapter(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public Mono<String> getToken(TenantId tenantId) {
        return redis.opsForValue().get(RedisKeyBuilder.bonificationTokenKey(tenantId));
    }

    @Override
    public Mono<Void> saveToken(TenantId tenantId, String token, Duration ttl) {
        return redis.opsForValue()
                .set(RedisKeyBuilder.bonificationTokenKey(tenantId), token, ttl)
                .then();
    }
}
