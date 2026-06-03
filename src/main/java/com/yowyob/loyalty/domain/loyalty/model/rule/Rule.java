package com.yowyob.loyalty.domain.loyalty.model.rule;

import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Rule {

    private final UUID id;
    private final TenantId tenantId;
    private final String name;
    private final String description;
    private final int priority;
    private final RuleStatus status;
    private final TriggerDefinition trigger;
    private final List<ConditionDefinition> conditions;
    private final List<EffectDefinition> effects;
    private final Instant validFrom;
    private final Instant validUntil;
    private final int version;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Rule(UUID id, TenantId tenantId, String name, String description, int priority, RuleStatus status,
                 TriggerDefinition trigger, List<ConditionDefinition> conditions, List<EffectDefinition> effects,
                 Instant validFrom, Instant validUntil, int version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.trigger = trigger;
        this.conditions = conditions;
        this.effects = effects;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Rule create(UUID id, TenantId tenantId, String name, String description,
                              TriggerDefinition trigger, List<ConditionDefinition> conditions,
                              List<EffectDefinition> effects, int priority, Instant validFrom, Instant validUntil) {
        if (trigger == null) {
            throw new IllegalArgumentException("Trigger definition is required");
        }
        if (effects == null || effects.isEmpty()) {
            throw new IllegalArgumentException("At least one effect is required");
        }

        Instant now = Instant.now();
        return new Rule(id, tenantId, name, description, priority, RuleStatus.DRAFT,
                trigger, conditions == null ? List.of() : conditions, effects,
                validFrom, validUntil, 0, now, now);
    }

    public static Rule reconstruct(UUID id, TenantId tenantId, String name, String description, int priority,
                                   RuleStatus status, TriggerDefinition trigger, List<ConditionDefinition> conditions,
                                   List<EffectDefinition> effects, Instant validFrom, Instant validUntil,
                                   int version, Instant createdAt, Instant updatedAt) {
        return new Rule(id, tenantId, name, description, priority, status, trigger, conditions, effects,
                validFrom, validUntil, version, createdAt, updatedAt);
    }

    public Rule activate() {
        return new Rule(id, tenantId, name, description, priority, RuleStatus.ACTIVE, trigger, conditions, effects,
                validFrom, validUntil, version + 1, createdAt, Instant.now());
    }

    public Rule suspend() {
        return new Rule(id, tenantId, name, description, priority, RuleStatus.SUSPENDED, trigger, conditions, effects,
                validFrom, validUntil, version + 1, createdAt, Instant.now());
    }

    public Rule archive() {
        return new Rule(id, tenantId, name, description, priority, RuleStatus.ARCHIVED, trigger, conditions, effects,
                validFrom, validUntil, version + 1, createdAt, Instant.now());
    }

    public boolean isActiveAt(Instant moment) {
        if (status != RuleStatus.ACTIVE) {
            return false;
        }
        if (validFrom != null && moment.isBefore(validFrom)) {
            return false;
        }
        if (validUntil != null && moment.isAfter(validUntil)) {
            return false;
        }
        return true;
    }

    public boolean triggerMatches(IncomingEvent event) {
        return trigger.matches(event);
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public TriggerDefinition getTrigger() {
        return trigger;
    }

    public List<ConditionDefinition> getConditions() {
        return conditions;
    }

    public List<EffectDefinition> getEffects() {
        return effects;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public int getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
