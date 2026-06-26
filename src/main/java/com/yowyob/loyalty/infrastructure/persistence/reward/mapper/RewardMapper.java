package com.yowyob.loyalty.infrastructure.persistence.reward.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.model.RewardStatus;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.model.RewardValue;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.reward.entity.RewardEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RewardMapper {

    private static final Logger log = LoggerFactory.getLogger(RewardMapper.class);
    private final ObjectMapper objectMapper;

    public RewardMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Reward toDomain(RewardEntity entity) {
        RewardValue value = readJson(entity.getValueJson(), RewardValue.class);
        Map<String, Object> metadata = readMap(entity.getMetadataJson());
        return Reward.reconstruct(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                entity.getDescription(),
                RewardType.valueOf(entity.getType()),
                value,
                entity.getCostInPoints(),
                entity.getStockTotal(),
                entity.getStockRemaining(),
                entity.getValidFrom(),
                entity.getValidUntil(),
                entity.getGrantExpiryDays(),
                entity.getImageUrl(),
                metadata,
                RewardStatus.valueOf(entity.getStatus()),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public RewardEntity toEntity(Reward reward) {
        RewardEntity entity = new RewardEntity();
        entity.setId(reward.id());
        entity.setTenantId(reward.tenantId().value());
        entity.setName(reward.name());
        entity.setDescription(reward.description());
        entity.setType(reward.type().name());
        entity.setValueJson(writeJson(reward.value()));
        entity.setCostInPoints(reward.costInPoints());
        entity.setStockTotal(reward.stockTotal());
        entity.setStockRemaining(reward.stockRemaining());
        entity.setValidFrom(reward.validFrom());
        entity.setValidUntil(reward.validUntil());
        entity.setGrantExpiryDays(reward.grantExpiryDays());
        entity.setImageUrl(reward.imageUrl());
        entity.setMetadataJson(writeJson(reward.metadata()));
        entity.setStatus(reward.status().name());
        entity.setVersion(reward.version());
        entity.setCreatedAt(reward.createdAt());
        entity.setUpdatedAt(reward.updatedAt());
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

    private Map<String, Object> readMap(String json) {
        try {
            if (json == null || json.isBlank()) return new HashMap<>();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
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
