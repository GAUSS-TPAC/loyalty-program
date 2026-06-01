package com.yowyob.loyaulty.program.domain.tenant.model;

import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantStatus;

public class Tenant {

    private final TenantId id;
    private final String name;
    private final String slug;
    private TenantStatus status;
    private TenantPlan plan;
    private TenantConfig config;
    private AuditInfo auditInfo;

    private Tenant(TenantId id, String name, String slug,
                   TenantStatus status, TenantPlan plan,
                   TenantConfig config, AuditInfo auditInfo) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.status = status;
        this.plan = plan;
        this.config = config;
        this.auditInfo = auditInfo;
    }

    public static Tenant create(TenantId id, String name, String slug, String createdBy) {
        return new Tenant(
                id, name, slug,
                TenantStatus.PENDING_SETUP,
                TenantPlan.FREE,
                TenantConfig.defaults(),
                AuditInfo.create(createdBy)
        );
    }

    public static Tenant reconstitute(TenantId id, String name, String slug,
                                      TenantStatus status, TenantPlan plan,
                                      TenantConfig config, AuditInfo auditInfo) {
        return new Tenant(id, name, slug, status, plan, config, auditInfo);
    }

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == TenantStatus.SUSPENDED;
    }

    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }

    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    public TenantId getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public TenantStatus getStatus() { return status; }
    public TenantPlan getPlan() { return plan; }
    public TenantConfig getConfig() { return config; }
    public AuditInfo getAuditInfo() { return auditInfo; }
}
