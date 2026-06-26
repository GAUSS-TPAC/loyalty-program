package com.yowyob.loyalty.infrastructure.persistence.reward.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.model.GrantStatus;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.model.RewardValue;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.reward.entity.RewardGrantEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RewardGrantMapper {

    private static final Logger log = LoggerFactory.getLogger(RewardGrantMapper.class);
    private final ObjectMapper objectMapper;

    public RewardGrantMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RewardGrant toDomain(RewardGrantEntity entity) {
        RewardValue value = readJson(entity.getRewardValueJson(), RewardValue.class);
        return RewardGrant.reconstruct(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                UserId.of(entity.getMemberId()),
                entity.getRewardId(),
                entity.getRewardName(),
                RewardType.valueOf(entity.getRewardType()),
                value,
                GrantSource.valueOf(entity.getSource()),
                entity.getSourceRuleId(),
                entity.getSourceEventId(),
                GrantStatus.valueOf(entity.getStatus()),
                entity.getRemainingApplications(),
                entity.getGrantedAt(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getUsedInContext(),
                entity.getVersion()
        );
    }

    public RewardGrantEntity toEntity(RewardGrant grant) {
        RewardGrantEntity entity = new RewardGrantEntity();
        entity.setId(grant.id());
        entity.setTenantId(grant.tenantId().value());
        entity.setMemberId(grant.memberId().value());
        entity.setRewardId(grant.rewardId());
        entity.setRewardName(grant.rewardName());
        entity.setRewardType(grant.rewardType().name());
        entity.setRewardValueJson(writeJson(grant.rewardValue()));
        entity.setSource(grant.source().name());
        entity.setSourceRuleId(grant.sourceRuleId());
        entity.setSourceEventId(grant.sourceEventId());
        entity.setStatus(grant.status().name());
        entity.setRemainingApplications(grant.remainingApplications());
        entity.setGrantedAt(grant.grantedAt());
        entity.setExpiresAt(grant.expiresAt());
        entity.setUsedAt(grant.usedAt());
        entity.setUsedInContext(grant.usedInContext());
        entity.setVersion(grant.version());
        return entity;
    }

    private <T> T readJson(String json, Class<T> type) {
        try {
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON: {}", e.getMessage());
            return null;
        }
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
