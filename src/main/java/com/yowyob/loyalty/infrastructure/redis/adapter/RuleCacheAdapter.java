package com.yowyob.loyalty.infrastructure.redis.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.port.out.RuleCachePort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.RuleEntity;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper.LoyaltyPersistenceMapper;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class RuleCacheAdapter implements RuleCachePort {

    private static final Logger log = LoggerFactory.getLogger(RuleCacheAdapter.class);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private static final List<String> COMMON_EVENT_TYPES = List.of(
            "purchase", "purchase.completed", "trip.ended", "member.enrolled"
    );

    private final ReactiveRedisTemplate<String, String> redis;
    private final ObjectMapper objectMapper;
    private final LoyaltyPersistenceMapper mapper;

    public RuleCacheAdapter(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis,
            ObjectMapper objectMapper,
            LoyaltyPersistenceMapper mapper
    ) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
    }

    @Override
    public List<Rule> getCachedRules(TenantId tenantId, String eventType) {
        try {
            String json = redis.opsForValue().get(RedisKeyBuilder.rulesKey(tenantId, eventType)).block();
            if (json == null || json.isBlank()) {
                return null;
            }
            List<RuleEntity> entities = objectMapper.readValue(json, new TypeReference<>() {});
            return entities.stream().map(mapper::toDomain).toList();
        } catch (Exception e) {
            log.warn("Failed to read rules cache: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void cacheRules(TenantId tenantId, String eventType, List<Rule> rules) {
        try {
            List<RuleEntity> entities = rules.stream().map(mapper::toEntity).toList();
            String json = objectMapper.writeValueAsString(entities);
            redis.opsForValue().set(RedisKeyBuilder.rulesKey(tenantId, eventType), json, DEFAULT_TTL).block();
        } catch (Exception e) {
            log.warn("Failed to write rules cache: {}", e.getMessage());
        }
    }

    @Override
    public void invalidateCache(TenantId tenantId) {
        for (String eventType : COMMON_EVENT_TYPES) {
            redis.delete(RedisKeyBuilder.rulesKey(tenantId, eventType)).block();
        }
    }
}
