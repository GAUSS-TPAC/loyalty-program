package com.yowyob.loyalty.infrastructure.persistence.loyalty.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsTransaction;
import com.yowyob.loyalty.domain.loyalty.model.rule.*;
import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class LoyaltyPersistenceMapper {

    private final ObjectMapper objectMapper;

    public LoyaltyPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RuleEntity toEntity(Rule rule) {
        RuleEntity entity = new RuleEntity();
        entity.setId(rule.getId());
        entity.setTenantId(rule.getTenantId().value());
        entity.setName(rule.getName());
        entity.setDescription(rule.getDescription());
        entity.setPriority(rule.getPriority());
        entity.setStatus(rule.getStatus().name());
        entity.setTriggerDefinition(writeJson(rule.getTrigger()));
        entity.setConditions(writeJson(rule.getConditions()));
        entity.setEffects(writeJson(rule.getEffects()));
        entity.setValidFrom(rule.getValidFrom());
        entity.setValidUntil(rule.getValidUntil());
        entity.setVersion(rule.getVersion());
        entity.setCreatedAt(rule.getCreatedAt());
        entity.setUpdatedAt(rule.getUpdatedAt());
        return entity;
    }

    public Rule toDomain(RuleEntity entity) {
        return Rule.reconstruct(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                entity.getDescription(),
                entity.getPriority(),
                RuleStatus.valueOf(entity.getStatus()),
                readJson(entity.getTriggerDefinition(), TriggerDefinition.class),
                readJsonList(entity.getConditions(), ConditionDefinition.class),
                readJsonList(entity.getEffects(), EffectDefinition.class),
                entity.getValidFrom(),
                entity.getValidUntil(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PointsAccountEntity toEntity(PointsAccount account) {
        PointsAccountEntity entity = new PointsAccountEntity();
        entity.setId(account.getId());
        entity.setTenantId(account.getTenantId().value());
        entity.setMemberId(account.getMemberId().value());
        entity.setAvailablePoints(account.getAvailablePoints());
        entity.setLifetimeEarned(account.getLifetimeEarned());
        entity.setLifetimeSpent(account.getLifetimeSpent());
        entity.setVersion(account.getVersion());
        entity.setLastActivityAt(account.getLastActivityAt());
        entity.setUpdatedAt(account.getUpdatedAt());
        return entity;
    }

    public PointsAccount toDomain(PointsAccountEntity entity) {
        return PointsAccount.reconstruct(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                UserId.of(entity.getMemberId()),
                entity.getAvailablePoints(),
                entity.getLifetimeEarned(),
                entity.getLifetimeSpent(),
                entity.getVersion(),
                entity.getLastActivityAt(),
                entity.getUpdatedAt()
        );
    }

    public PointsTransactionEntity toEntity(PointsTransaction tx) {
        PointsTransactionEntity entity = new PointsTransactionEntity();
        entity.setId(tx.id());
        entity.setPointsAccountId(tx.pointsAccountId());
        entity.setTenantId(tx.tenantId().value());
        entity.setType(tx.type());
        entity.setAmount(tx.amount());
        entity.setBalanceAfter(tx.balanceAfter());
        entity.setSource(tx.source());
        entity.setRuleId(tx.ruleId());
        entity.setEventIdempotencyKey(tx.eventIdempotencyKey());
        entity.setMetadata(writeJson(tx.metadata()));
        entity.setCreatedAt(tx.createdAt());
        return entity;
    }

    public PointsTransaction toDomain(PointsTransactionEntity entity) {
        return new PointsTransaction(
                entity.getId(),
                entity.getPointsAccountId(),
                TenantId.of(entity.getTenantId()),
                entity.getType(),
                entity.getAmount(),
                entity.getBalanceAfter(),
                entity.getSource(),
                entity.getRuleId(),
                entity.getEventIdempotencyKey(),
                readJsonMap(entity.getMetadata()),
                entity.getCreatedAt()
        );
    }

    public CounterEntity toEntity(Counter counter) {
        CounterEntity entity = new CounterEntity();
        entity.setId(counter.id());
        entity.setTenantId(counter.tenantId().value());
        entity.setMemberId(counter.memberId().value());
        entity.setCounterKey(counter.counterKey());
        entity.setValue(counter.value());
        entity.setWindowType(counter.windowType());
        entity.setWindowStart(counter.windowStart());
        entity.setUpdatedAt(counter.updatedAt());
        return entity;
    }

    public Counter toDomain(CounterEntity entity) {
        return new Counter(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                UserId.of(entity.getMemberId()),
                entity.getCounterKey(),
                entity.getValue(),
                entity.getWindowType(),
                entity.getWindowStart(),
                entity.getUpdatedAt()
        );
    }

    public MemberTierEntity toEntity(MemberTier tier) {
        MemberTierEntity entity = new MemberTierEntity();
        entity.setId(tier.id());
        entity.setTenantId(tier.tenantId().value());
        entity.setMemberId(tier.memberId().value());
        entity.setTierLevel(tier.level().name());
        entity.setMultiplier(tier.pointsMultiplier());
        entity.setReachedAt(tier.reachedAt());
        entity.setValidUntil(tier.validUntil());
        return entity;
    }

    public MemberTier toDomain(MemberTierEntity entity) {
        return new MemberTier(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                UserId.of(entity.getMemberId()),
                TierLevel.valueOf(entity.getTierLevel()),
                entity.getMultiplier(),
                entity.getReachedAt(),
                entity.getValidUntil()
        );
    }

    public TierPolicyEntity toEntity(TierPolicy policy) {
        TierPolicyEntity entity = new TierPolicyEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(policy.tenantId().value());
        entity.setCriterion(policy.criterion());
        entity.setThresholds(writeJson(policy.thresholds()));
        entity.setMaintainPeriod(policy.maintainPeriod());
        entity.setMaintainThresholdPoints(policy.maintainThresholdPoints());
        entity.setDowngradeGraceDays(policy.downgradeGraceDays());
        return entity;
    }

    public TierPolicy toDomain(TierPolicyEntity entity) {
        return new TierPolicy(
                TenantId.of(entity.getTenantId()),
                entity.getCriterion(),
                readJsonList(entity.getThresholds(), TierPolicy.TierThreshold.class),
                entity.getMaintainPeriod(),
                entity.getMaintainThresholdPoints(),
                entity.getDowngradeGraceDays()
        );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

    private <T> T readJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON deserialization failed", e);
        }
    }

    private <T> List<T> readJsonList(String json, Class<T> elementType) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON list deserialization failed", e);
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON map deserialization failed", e);
        }
    }
}
