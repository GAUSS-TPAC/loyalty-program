package com.yowyob.loyalty.infrastructure.redis.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class TenantCacheAdapter {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public TenantCacheAdapter(@Qualifier("objectRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Tenant> findById(TenantId id) {
        String key = RedisKeyBuilder.tenantCacheKey(id);
        return redisTemplate.opsForValue().get(key)
                .cast(Tenant.class);
    }

    public Mono<Void> cache(Tenant tenant) {
        String key = RedisKeyBuilder.tenantCacheKey(tenant.getId());
        return redisTemplate.opsForValue().set(key, tenant, Duration.ofMinutes(5))
                .then();
    }

    public Mono<Void> evict(TenantId id) {
        String key = RedisKeyBuilder.tenantCacheKey(id);
        return redisTemplate.opsForValue().delete(key)
                .then();
    }
}
