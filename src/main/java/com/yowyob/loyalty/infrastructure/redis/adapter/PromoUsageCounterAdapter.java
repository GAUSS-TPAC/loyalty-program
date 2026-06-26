package com.yowyob.loyalty.infrastructure.redis.adapter;

import com.yowyob.loyalty.domain.promo.port.out.PromoUsageCounterPort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PromoUsageCounterAdapter implements PromoUsageCounterPort {

    private static final Logger log = LoggerFactory.getLogger(PromoUsageCounterAdapter.class);

    private final ReactiveRedisTemplate<String, String> redis;

    public PromoUsageCounterAdapter(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Long> increment(TenantId tenantId, UUID campaignId) {
        String key = RedisKeyBuilder.promoCampaignCounterKey(tenantId, campaignId);
        return redis.opsForValue().increment(key)
                .onErrorResume(e -> {
                    log.warn("Redis increment failed for promo counter {}: {}", key, e.getMessage());
                    return Mono.just(0L);
                });
    }

    @Override
    public Mono<Long> getCount(TenantId tenantId, UUID campaignId) {
        String key = RedisKeyBuilder.promoCampaignCounterKey(tenantId, campaignId);
        return redis.opsForValue().get(key)
                .map(v -> v == null ? 0L : Long.parseLong(v))
                .onErrorReturn(0L);
    }

    @Override
    public Mono<Void> reset(TenantId tenantId, UUID campaignId) {
        String key = RedisKeyBuilder.promoCampaignCounterKey(tenantId, campaignId);
        return redis.delete(key).then();
    }
}
