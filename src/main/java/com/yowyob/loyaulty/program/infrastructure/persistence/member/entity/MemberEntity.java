package com.yowyob.loyaulty.program.infrastructure.persistence.member.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("members")
public class MemberEntity {
    @Id private UUID id;
    @Column("tenant_id")    private UUID tenantId;
    @Column("external_id")  private String externalId;
    @Column("email")        private String email;
    @Column("phone")        private String phone;
    @Column("display_name") private String displayName;
    @Column("status")       private String status;
    @Column("created_at")   private Instant createdAt;
    @Column("updated_at")   private Instant updatedAt;
    @Column("created_by")   private String createdBy;
    @Column("updated_by")   private String updatedBy;

    public UUID getId()                    { return id; }
    public void setId(UUID id)             { this.id = id; }
    public UUID getTenantId()              { return tenantId; }
    public void setTenantId(UUID v)        { this.tenantId = v; }
    public String getExternalId()          { return externalId; }
    public void setExternalId(String v)    { this.externalId = v; }
    public String getEmail()               { return email; }
    public void setEmail(String v)         { this.email = v; }
    public String getPhone()               { return phone; }
    public void setPhone(String v)         { this.phone = v; }
    public String getDisplayName()         { return displayName; }
    public void setDisplayName(String v)   { this.displayName = v; }
    public String getStatus()              { return status; }
    public void setStatus(String v)        { this.status = v; }
    public Instant getCreatedAt()          { return createdAt; }
    public void setCreatedAt(Instant v)    { this.createdAt = v; }
    public Instant getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(Instant v)    { this.updatedAt = v; }
    public String getCreatedBy()           { return createdBy; }
    public void setCreatedBy(String v)     { this.createdBy = v; }
    public String getUpdatedBy()           { return updatedBy; }
    public void setUpdatedBy(String v)     { this.updatedBy = v; }
}
