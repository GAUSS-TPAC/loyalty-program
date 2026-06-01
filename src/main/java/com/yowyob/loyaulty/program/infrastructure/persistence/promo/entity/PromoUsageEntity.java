package com.yowyob.loyaulty.program.infrastructure.persistence.promo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("promo_usages")
public class PromoUsageEntity {
    @Id private UUID id;
    @Column("tenant_id")        private UUID tenantId;
    @Column("campaign_id")      private UUID campaignId;
    @Column("member_id")        private String memberId;
    @Column("order_reference")  private String orderReference;
    @Column("discount_applied") private BigDecimal discountApplied;
    @Column("used_at")          private Instant usedAt;

    public UUID getId()                      { return id; }
    public void setId(UUID v)                { this.id = v; }
    public UUID getTenantId()                { return tenantId; }
    public void setTenantId(UUID v)          { this.tenantId = v; }
    public UUID getCampaignId()              { return campaignId; }
    public void setCampaignId(UUID v)        { this.campaignId = v; }
    public String getMemberId()              { return memberId; }
    public void setMemberId(String v)        { this.memberId = v; }
    public String getOrderReference()        { return orderReference; }
    public void setOrderReference(String v)  { this.orderReference = v; }
    public BigDecimal getDiscountApplied()   { return discountApplied; }
    public void setDiscountApplied(BigDecimal v){ this.discountApplied = v; }
    public Instant getUsedAt()               { return usedAt; }
    public void setUsedAt(Instant v)         { this.usedAt = v; }
}
