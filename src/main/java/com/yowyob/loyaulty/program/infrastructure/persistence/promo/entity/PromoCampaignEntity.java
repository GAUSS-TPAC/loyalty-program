package com.yowyob.loyaulty.program.infrastructure.persistence.promo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("promo_campaigns")
public class PromoCampaignEntity {
    @Id private UUID id;
    @Column("tenant_id")           private UUID tenantId;
    @Column("name")                private String name;
    @Column("code")                private String code;
    @Column("discount_type")       private String discountType;
    @Column("discount_value")      private BigDecimal discountValue;
    @Column("status")              private String status;
    @Column("max_uses_total")      private Integer maxUsesTotal;
    @Column("max_uses_per_member") private Integer maxUsesPerMember;
    @Column("min_order_amount")    private BigDecimal minOrderAmount;
    @Column("valid_from")          private Instant validFrom;
    @Column("valid_until")         private Instant validUntil;
    @Column("created_at")          private Instant createdAt;

    public UUID getId()                      { return id; }
    public void setId(UUID v)                { this.id = v; }
    public UUID getTenantId()                { return tenantId; }
    public void setTenantId(UUID v)          { this.tenantId = v; }
    public String getName()                  { return name; }
    public void setName(String v)            { this.name = v; }
    public String getCode()                  { return code; }
    public void setCode(String v)            { this.code = v; }
    public String getDiscountType()          { return discountType; }
    public void setDiscountType(String v)    { this.discountType = v; }
    public BigDecimal getDiscountValue()     { return discountValue; }
    public void setDiscountValue(BigDecimal v){ this.discountValue = v; }
    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }
    public Integer getMaxUsesTotal()         { return maxUsesTotal; }
    public void setMaxUsesTotal(Integer v)   { this.maxUsesTotal = v; }
    public Integer getMaxUsesPerMember()     { return maxUsesPerMember; }
    public void setMaxUsesPerMember(Integer v){ this.maxUsesPerMember = v; }
    public BigDecimal getMinOrderAmount()    { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal v){ this.minOrderAmount = v; }
    public Instant getValidFrom()            { return validFrom; }
    public void setValidFrom(Instant v)      { this.validFrom = v; }
    public Instant getValidUntil()           { return validUntil; }
    public void setValidUntil(Instant v)     { this.validUntil = v; }
    public Instant getCreatedAt()            { return createdAt; }
    public void setCreatedAt(Instant v)      { this.createdAt = v; }
}
