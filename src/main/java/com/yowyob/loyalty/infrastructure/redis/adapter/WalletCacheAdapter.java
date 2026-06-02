package com.yowyob.loyalty.infrastructure.redis.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Component
public class WalletCacheAdapter {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public WalletCacheAdapter(ReactiveRedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> cacheBalance(TenantId tenantId, UserId memberId, BigDecimal balance) {
        String key = RedisKeyBuilder.walletBalanceKey(tenantId, memberId);
        return redisTemplate.opsForValue().set(key, balance.toString(), Duration.ofSeconds(30)).then();
    }

    public Mono<Optional<BigDecimal>> getBalance(TenantId tenantId, UserId memberId) {
        String key = RedisKeyBuilder.walletBalanceKey(tenantId, memberId);
        return redisTemplate.opsForValue().get(key)
            .map(val -> Optional.of(new BigDecimal(val)))
            .defaultIfEmpty(Optional.empty());
    }

    public Mono<Void> evictBalance(TenantId tenantId, UserId memberId) {
        String key = RedisKeyBuilder.walletBalanceKey(tenantId, memberId);
        return redisTemplate.opsForValue().delete(key).then();
    }
}
