package com.yowyob.loyaulty.program.domain.member.model;

import com.yowyob.loyaulty.program.domain.member.model.enums.MemberStatus;
import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.util.UUID;

public class Member {

    private final UUID id;
    private final TenantId tenantId;
    private final String externalId;
    private String email;
    private String phone;
    private String displayName;
    private MemberStatus status;
    private final AuditInfo auditInfo;

    private Member(UUID id, TenantId tenantId, String externalId,
                   String email, String phone, String displayName,
                   MemberStatus status, AuditInfo auditInfo) {
        this.id = id;
        this.tenantId = tenantId;
        this.externalId = externalId;
        this.email = email;
        this.phone = phone;
        this.displayName = displayName;
        this.status = status;
        this.auditInfo = auditInfo;
    }

    public static Member enroll(TenantId tenantId, String externalId,
                                String email, String phone, String displayName) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("externalId is required");
        }
        return new Member(UUID.randomUUID(), tenantId, externalId,
                email, phone, displayName, MemberStatus.ACTIVE,
                AuditInfo.create("system"));
    }

    public static Member reconstitute(UUID id, TenantId tenantId, String externalId,
                                      String email, String phone, String displayName,
                                      MemberStatus status, AuditInfo auditInfo) {
        return new Member(id, tenantId, externalId, email, phone, displayName, status, auditInfo);
    }

    public void block() {
        this.status = MemberStatus.BLOCKED;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    public boolean isActive() { return status == MemberStatus.ACTIVE; }

    public UUID getId()            { return id; }
    public TenantId getTenantId()  { return tenantId; }
    public String getExternalId()  { return externalId; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public String getDisplayName() { return displayName; }
    public MemberStatus getStatus(){ return status; }
    public AuditInfo getAuditInfo(){ return auditInfo; }
}
