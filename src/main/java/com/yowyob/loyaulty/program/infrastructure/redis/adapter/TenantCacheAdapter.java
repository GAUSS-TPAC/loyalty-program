package com.yowyob.loyaulty.program.infrastructure.redis.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.tenant.model.Tenant;
import com.yowyob.loyaulty.program.domain.tenant.model.TenantConfig;
import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantStatus;
import com.yowyob.loyaulty.program.infrastructure.persistence.tenant.adapter.TenantRepositoryAdapter;
import com.yowyob.loyaulty.program.shared.util.RedisKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
public class TenantCacheAdapter {

    private static final Logger log = LoggerFactory.getLogger(TenantCacheAdapter.class);
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ReactiveRedisTemplate<String, String> redis;
    private final TenantRepositoryAdapter dbAdapter;
    private final ObjectMapper mapper;

    public TenantCacheAdapter(ReactiveRedisTemplate<String, String> redis,
                               TenantRepositoryAdapter dbAdapter) {
        this.redis = redis;
        this.dbAdapter = dbAdapter;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Mono<Tenant> findById(TenantId tenantId) {
        String key = RedisKeyBuilder.tenantKey(tenantId);
        return redis.opsForValue().get(key)
                .flatMap(json -> deserialize(json, tenantId))
                .switchIfEmpty(
                        dbAdapter.findById(tenantId)
                                .flatMap(tenant -> cache(key, tenant).thenReturn(tenant))
                )
                .doOnError(e -> log.warn("Cache miss error for tenant {}: {}", tenantId, e.getMessage()));
    }

    public Mono<Void> evict(TenantId tenantId) {
        return redis.opsForValue().delete(RedisKeyBuilder.tenantKey(tenantId)).then();
    }

    private Mono<Void> cache(String key, Tenant tenant) {
        try {
            TenantCacheDto dto = TenantCacheDto.from(tenant);
            String json = mapper.writeValueAsString(dto);
            return redis.opsForValue().set(key, json, TTL).then();
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache tenant: {}", e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Tenant> deserialize(String json, TenantId tenantId) {
        try {
            TenantCacheDto dto = mapper.readValue(json, TenantCacheDto.class);
            return Mono.just(dto.toTenant());
        } catch (Exception e) {
            log.warn("Failed to deserialize cached tenant {}: {}", tenantId, e.getMessage());
            return Mono.empty();
        }
    }

    // Simple DTO for caching — keeps domain model clean
    record TenantCacheDto(
            String id, String name, String slug,
            String status, String plan,
            String currencyCode, String currencyName, String currencySymbol,
            double exchangeRate, boolean walletAutoActivate, int pointExpiryDays
    ) {
        static TenantCacheDto from(Tenant t) {
            TenantConfig cfg = t.getConfig();
            return new TenantCacheDto(
                    t.getId().value().toString(), t.getName(), t.getSlug(),
                    t.getStatus().name(), t.getPlan().name(),
                    cfg.defaultCurrencyCode(), cfg.virtualCurrencyName(),
                    cfg.virtualCurrencySymbol(), cfg.exchangeRate(),
                    cfg.walletAutoActivate(), cfg.pointExpiryDays()
            );
        }

        Tenant toTenant() {
            TenantConfig cfg = new TenantConfig(
                    currencyCode, currencyName, currencySymbol,
                    exchangeRate, walletAutoActivate, pointExpiryDays,
                    java.util.List.of()
            );
            AuditInfo audit = AuditInfo.create("system");
            return Tenant.reconstitute(
                    TenantId.of(UUID.fromString(id)), name, slug,
                    TenantStatus.valueOf(status), TenantPlan.valueOf(plan),
                    cfg, audit
            );
        }
    }
}
