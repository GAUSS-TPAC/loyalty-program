package com.yowyob.loyaulty.program.infrastructure.persistence.tenant.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.tenant.model.Tenant;
import com.yowyob.loyaulty.program.domain.tenant.model.TenantConfig;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantStatus;
import com.yowyob.loyaulty.program.infrastructure.persistence.tenant.entity.TenantEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TenantMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Tenant toDomain(TenantEntity entity) {
        TenantConfig config = parseConfig(entity.getConfig());
        AuditInfo auditInfo = new AuditInfo(
                entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getCreatedBy(), entity.getUpdatedBy()
        );
        return Tenant.reconstitute(
                TenantId.of(entity.getId()),
                entity.getName(),
                entity.getSlug(),
                TenantStatus.valueOf(entity.getStatus()),
                TenantPlan.valueOf(entity.getPlan()),
                config,
                auditInfo
        );
    }

    public TenantEntity toEntity(Tenant tenant) {
        TenantEntity entity = new TenantEntity();
        entity.setId(tenant.getId().value());
        entity.setName(tenant.getName());
        entity.setSlug(tenant.getSlug());
        entity.setStatus(tenant.getStatus().name());
        entity.setPlan(tenant.getPlan().name());
        entity.setConfig(serializeConfig(tenant.getConfig()));
        entity.setCreatedAt(tenant.getAuditInfo().createdAt());
        entity.setUpdatedAt(tenant.getAuditInfo().updatedAt());
        entity.setCreatedBy(tenant.getAuditInfo().createdBy());
        entity.setUpdatedBy(tenant.getAuditInfo().updatedBy());
        return entity;
    }

    @SuppressWarnings("unchecked")
    private TenantConfig parseConfig(String json) {
        if (json == null || json.isBlank()) return TenantConfig.defaults();
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            return new TenantConfig(
                    (String) map.getOrDefault("defaultCurrencyCode", "XAF"),
                    (String) map.getOrDefault("virtualCurrencyName", "Points"),
                    (String) map.getOrDefault("virtualCurrencySymbol", "PTS"),
                    ((Number) map.getOrDefault("exchangeRate", 1.0)).doubleValue(),
                    (boolean) map.getOrDefault("walletAutoActivate", true),
                    ((Number) map.getOrDefault("pointExpiryDays", 365)).intValue(),
                    (List<String>) map.getOrDefault("notificationChannels", List.of("EMAIL"))
            );
        } catch (Exception e) {
            return TenantConfig.defaults();
        }
    }

    private String serializeConfig(TenantConfig config) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "defaultCurrencyCode", config.defaultCurrencyCode(),
                    "virtualCurrencyName", config.virtualCurrencyName(),
                    "virtualCurrencySymbol", config.virtualCurrencySymbol(),
                    "exchangeRate", config.exchangeRate(),
                    "walletAutoActivate", config.walletAutoActivate(),
                    "pointExpiryDays", config.pointExpiryDays(),
                    "notificationChannels", config.notificationChannels()
            ));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
