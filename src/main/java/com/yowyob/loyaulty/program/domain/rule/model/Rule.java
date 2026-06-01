package com.yowyob.loyaulty.program.domain.rule.model;

import com.yowyob.loyaulty.program.domain.rule.model.enums.ConditionType;
import com.yowyob.loyaulty.program.domain.rule.model.enums.RuleStatus;
import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Rule {

    private final UUID id;
    private final TenantId tenantId;
    private final String name;
    private final String description;
    private final int priority;
    private RuleStatus status;
    private final Trigger trigger;
    private final List<Condition> conditions;
    private final List<Effect> effects;
    private final Instant validFrom;
    private final Instant validUntil;
    private final AuditInfo auditInfo;

    private Rule(UUID id, TenantId tenantId, String name, String description,
                 int priority, RuleStatus status, Trigger trigger,
                 List<Condition> conditions, List<Effect> effects,
                 Instant validFrom, Instant validUntil, AuditInfo auditInfo) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.trigger = trigger;
        this.conditions = conditions != null ? conditions : List.of();
        this.effects = effects != null ? effects : List.of();
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.auditInfo = auditInfo;
    }

    public static Rule create(TenantId tenantId, String name, String description,
                               int priority, Trigger trigger,
                               List<Condition> conditions, List<Effect> effects,
                               Instant validFrom, Instant validUntil) {
        return new Rule(UUID.randomUUID(), tenantId, name, description,
                priority, RuleStatus.DRAFT, trigger, conditions, effects,
                validFrom, validUntil, AuditInfo.create("system"));
    }

    public static Rule reconstitute(UUID id, TenantId tenantId, String name, String description,
                                     int priority, RuleStatus status, Trigger trigger,
                                     List<Condition> conditions, List<Effect> effects,
                                     Instant validFrom, Instant validUntil, AuditInfo auditInfo) {
        return new Rule(id, tenantId, name, description, priority, status,
                trigger, conditions, effects, validFrom, validUntil, auditInfo);
    }

    // ── Évaluation ────────────────────────────────────────────────────────

    public boolean isActiveAt(Instant now) {
        if (status != RuleStatus.ACTIVE) return false;
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
        return true;
    }

    public boolean triggerMatches(String eventType, Map<String, Object> payload) {
        return trigger.matches(eventType, payload);
    }

    public boolean conditionsMet(Map<UUID, Long> counters) {
        return conditions.stream().allMatch(cond -> evaluateCondition(cond, counters));
    }

    private boolean evaluateCondition(Condition cond, Map<UUID, Long> counters) {
        return switch (cond.type()) {
            case CUMULATIVE_COUNT -> {
                long count = counters.getOrDefault(this.id, 0L);
                yield cond.evaluate(count);
            }
            // Les autres types nécessitent des données supplémentaires (palier, solde)
            // Pour l'instant, on les considère comme toujours vraies
            default -> true;
        };
    }

    // ── Cycle de vie ──────────────────────────────────────────────────────

    public void activate()  { this.status = RuleStatus.ACTIVE; }
    public void suspend()   { this.status = RuleStatus.SUSPENDED; }
    public void archive()   { this.status = RuleStatus.ARCHIVED; }

    // ── Accesseurs ────────────────────────────────────────────────────────

    public UUID getId()               { return id; }
    public TenantId getTenantId()     { return tenantId; }
    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public int getPriority()          { return priority; }
    public RuleStatus getStatus()     { return status; }
    public Trigger getTrigger()       { return trigger; }
    public List<Condition> getConditions() { return conditions; }
    public List<Effect> getEffects()  { return effects; }
    public Instant getValidFrom()     { return validFrom; }
    public Instant getValidUntil()    { return validUntil; }
    public AuditInfo getAuditInfo()   { return auditInfo; }
}
