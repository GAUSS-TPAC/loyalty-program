package com.yowyob.loyaulty.program.domain.promo.model;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PromoUsage {

    private final UUID id;
    private final TenantId tenantId;
    private final UUID campaignId;
    private final String memberId;
    private final String orderReference;
    private final BigDecimal discountApplied;
    private final Instant usedAt;

    private PromoUsage(UUID id, TenantId tenantId, UUID campaignId, String memberId,
                       String orderReference, BigDecimal discountApplied, Instant usedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.campaignId = campaignId;
        this.memberId = memberId;
        this.orderReference = orderReference;
        this.discountApplied = discountApplied;
        this.usedAt = usedAt;
    }

    public static PromoUsage record(TenantId tenantId, UUID campaignId, String memberId,
                                     String orderReference, BigDecimal discountApplied) {
        return new PromoUsage(UUID.randomUUID(), tenantId, campaignId, memberId,
                orderReference, discountApplied, Instant.now());
    }

    public static PromoUsage reconstitute(UUID id, TenantId tenantId, UUID campaignId,
                                           String memberId, String orderReference,
                                           BigDecimal discountApplied, Instant usedAt) {
        return new PromoUsage(id, tenantId, campaignId, memberId,
                orderReference, discountApplied, usedAt);
    }

    public UUID getId()                   { return id; }
    public TenantId getTenantId()         { return tenantId; }
    public UUID getCampaignId()           { return campaignId; }
    public String getMemberId()           { return memberId; }
    public String getOrderReference()     { return orderReference; }
    public BigDecimal getDiscountApplied(){ return discountApplied; }
    public Instant getUsedAt()            { return usedAt; }
}
