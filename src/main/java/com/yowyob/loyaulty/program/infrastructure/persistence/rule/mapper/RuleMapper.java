package com.yowyob.loyaulty.program.infrastructure.persistence.rule.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyaulty.program.domain.rule.model.*;
import com.yowyob.loyaulty.program.domain.rule.model.enums.RuleStatus;
import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.rule.entity.RuleEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RuleMapper {

    private static final TypeReference<List<Condition>> COND_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Effect>>    EFF_TYPE  = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public RuleMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Rule toDomain(RuleEntity e) {
        return Rule.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId().toString()),
                e.getName(),
                e.getDescription(),
                e.getPriority(),
                RuleStatus.valueOf(e.getStatus()),
                fromJson(e.getTriggerDef(), Trigger.class),
                fromJsonList(e.getConditionsDef(), COND_TYPE),
                fromJsonList(e.getEffectsDef(), EFF_TYPE),
                e.getValidFrom(),
                e.getValidUntil(),
                new AuditInfo(e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getCreatedBy())
        );
    }

    public RuleEntity toEntity(Rule r) {
        RuleEntity e = new RuleEntity();
        e.setId(r.getId());
        e.setTenantId(r.getTenantId().value());
        e.setName(r.getName());
        e.setDescription(r.getDescription());
        e.setPriority(r.getPriority());
        e.setStatus(r.getStatus().name());
        e.setTriggerDef(toJson(r.getTrigger()));
        e.setConditionsDef(toJson(r.getConditions()));
        e.setEffectsDef(toJson(r.getEffects()));
        e.setValidFrom(r.getValidFrom());
        e.setValidUntil(r.getValidUntil());
        e.setCreatedAt(r.getAuditInfo().createdAt());
        e.setUpdatedAt(Instant.now());
        e.setCreatedBy(r.getAuditInfo().createdBy());
        return e;
    }

    private <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readValue(json, type); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Cannot deserialize: " + json, ex); }
    }

    private <T> T fromJsonList(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readValue(json, type); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Cannot deserialize: " + json, ex); }
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Cannot serialize", ex); }
    }
}
