package com.yowyob.loyaulty.program.domain.promo.model;

import com.yowyob.loyaulty.program.domain.promo.model.enums.DiscountType;
import com.yowyob.loyaulty.program.domain.promo.model.enums.PromoStatus;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PromoCampaign {

    private final UUID id;
    private final TenantId tenantId;
    private String name;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private PromoStatus status;
    private Integer maxUsesTotal;
    private Integer maxUsesPerMember;
    private BigDecimal minOrderAmount;
    private Instant validFrom;
    private Instant validUntil;
    private final Instant createdAt;

    private PromoCampaign(UUID id, TenantId tenantId, String name, String code,
                          DiscountType discountType, BigDecimal discountValue,
                          PromoStatus status, Integer maxUsesTotal, Integer maxUsesPerMember,
                          BigDecimal minOrderAmount, Instant validFrom, Instant validUntil,
                          Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.status = status;
        this.maxUsesTotal = maxUsesTotal;
        this.maxUsesPerMember = maxUsesPerMember;
        this.minOrderAmount = minOrderAmount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
    }

    public static PromoCampaign create(TenantId tenantId, String name, String code,
                                        DiscountType discountType, BigDecimal discountValue,
                                        Integer maxUsesTotal, Integer maxUsesPerMember,
                                        BigDecimal minOrderAmount,
                                        Instant validFrom, Instant validUntil) {
        return new PromoCampaign(
                UUID.randomUUID(), tenantId, name, code,
                discountType, discountValue,
                PromoStatus.DRAFT, maxUsesTotal, maxUsesPerMember,
                minOrderAmount, validFrom, validUntil, Instant.now()
        );
    }

    public static PromoCampaign reconstitute(UUID id, TenantId tenantId, String name, String code,
                                              DiscountType discountType, BigDecimal discountValue,
                                              PromoStatus status, Integer maxUsesTotal,
                                              Integer maxUsesPerMember, BigDecimal minOrderAmount,
                                              Instant validFrom, Instant validUntil,
                                              Instant createdAt) {
        return new PromoCampaign(id, tenantId, name, code, discountType, discountValue,
                status, maxUsesTotal, maxUsesPerMember, minOrderAmount,
                validFrom, validUntil, createdAt);
    }

    /**
     * Valide si ce code promo peut être appliqué.
     * Retourne une raison d'échec ou null si valide.
     */
    public String validate(BigDecimal orderAmount, Instant now) {
        if (status != PromoStatus.ACTIVE) return "Promo is not active";
        if (validFrom != null && now.isBefore(validFrom)) return "Promo not yet started";
        if (validUntil != null && now.isAfter(validUntil)) return "Promo has expired";
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0)
            return "Order amount below minimum: " + minOrderAmount;
        return null;
    }

    /**
     * Calcule la valeur de réduction à appliquer sur {@code orderAmount}.
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return switch (discountType) {
            case PERCENT -> orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
            case FIXED_AMOUNT, WALLET_CREDIT -> discountValue.min(orderAmount);
            case FREE_PRODUCT -> BigDecimal.ZERO;
        };
    }

    public void activate() { this.status = PromoStatus.ACTIVE; }
    public void pause()    { this.status = PromoStatus.PAUSED; }

    public UUID getId()                  { return id; }
    public TenantId getTenantId()        { return tenantId; }
    public String getName()              { return name; }
    public String getCode()              { return code; }
    public DiscountType getDiscountType(){ return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public PromoStatus getStatus()       { return status; }
    public Integer getMaxUsesTotal()     { return maxUsesTotal; }
    public Integer getMaxUsesPerMember() { return maxUsesPerMember; }
    public BigDecimal getMinOrderAmount(){ return minOrderAmount; }
    public Instant getValidFrom()        { return validFrom; }
    public Instant getValidUntil()       { return validUntil; }
    public Instant getCreatedAt()        { return createdAt; }
}
