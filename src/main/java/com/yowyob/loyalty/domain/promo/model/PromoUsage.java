package com.yowyob.loyalty.domain.promo.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PromoUsage {

    private final UUID id;
    private final TenantId tenantId;
    private final UUID campaignId;
    private final UserId memberId;
    private final String orderId;
    private final BigDecimal discountApplied;
    private final Instant usedAt;

    private PromoUsage(UUID id, TenantId tenantId, UUID campaignId, UserId memberId,
                       String orderId, BigDecimal discountApplied, Instant usedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.campaignId = campaignId;
        this.memberId = memberId;
        this.orderId = orderId;
        this.discountApplied = discountApplied;
        this.usedAt = usedAt;
    }

    public static PromoUsage record(TenantId tenantId, UUID campaignId, UserId memberId,
                                    String orderId, BigDecimal discountApplied) {
        return new PromoUsage(UUID.randomUUID(), tenantId, campaignId, memberId,
                orderId, discountApplied, Instant.now());
    }

    public static PromoUsage reconstruct(UUID id, TenantId tenantId, UUID campaignId, UserId memberId,
                                         String orderId, BigDecimal discountApplied, Instant usedAt) {
        return new PromoUsage(id, tenantId, campaignId, memberId, orderId, discountApplied, usedAt);
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public UUID campaignId() { return campaignId; }
    public UserId memberId() { return memberId; }
    public String orderId() { return orderId; }
    public BigDecimal discountApplied() { return discountApplied; }
    public Instant usedAt() { return usedAt; }
}
