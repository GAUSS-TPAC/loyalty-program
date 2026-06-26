package com.yowyob.loyalty.domain.promo.model;

import com.yowyob.loyalty.domain.promo.exception.PromoDomainException;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PromoCampaign {

    private final UUID id;
    private final TenantId tenantId;
    private final String code;
    private String name;
    private final PromoDiscountType discountType;
    private final BigDecimal discountValue;
    private final BigDecimal minOrderAmount;
    private final int maxUses;
    private final int perMemberLimit;
    private final Instant startDate;
    private final Instant endDate;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private PromoCampaign(UUID id, TenantId tenantId, String code, String name,
                          PromoDiscountType discountType, BigDecimal discountValue,
                          BigDecimal minOrderAmount, int maxUses, int perMemberLimit,
                          Instant startDate, Instant endDate, boolean active,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxUses = maxUses;
        this.perMemberLimit = perMemberLimit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PromoCampaign create(UUID id, TenantId tenantId, String code, String name,
                                       PromoDiscountType discountType, BigDecimal discountValue,
                                       BigDecimal minOrderAmount, int maxUses, int perMemberLimit,
                                       Instant startDate, Instant endDate) {
        if (code == null || code.isBlank())
            throw new PromoDomainException("Le code promo est obligatoire");
        if (name == null || name.isBlank())
            throw new PromoDomainException("Le nom de la campagne est obligatoire");
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0)
            throw new PromoDomainException("La valeur de remise doit être positive");
        if (discountType == PromoDiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new PromoDomainException("Un pourcentage de remise ne peut pas dépasser 100%");
        if (startDate == null)
            throw new PromoDomainException("La date de début est obligatoire");
        if (endDate != null && endDate.isBefore(startDate))
            throw new PromoDomainException("La date de fin doit être après la date de début");

        Instant now = Instant.now();
        return new PromoCampaign(id, tenantId, code.toUpperCase().trim(), name,
                discountType, discountValue,
                minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO,
                maxUses, perMemberLimit, startDate, endDate, false, now, now);
    }

    public static PromoCampaign reconstruct(UUID id, TenantId tenantId, String code, String name,
                                            PromoDiscountType discountType, BigDecimal discountValue,
                                            BigDecimal minOrderAmount, int maxUses, int perMemberLimit,
                                            Instant startDate, Instant endDate, boolean active,
                                            Instant createdAt, Instant updatedAt) {
        return new PromoCampaign(id, tenantId, code, name, discountType, discountValue,
                minOrderAmount, maxUses, perMemberLimit, startDate, endDate, active, createdAt, updatedAt);
    }

    public PromoCampaign activate() {
        this.active = true;
        this.updatedAt = Instant.now();
        return this;
    }

    public PromoCampaign deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
        return this;
    }

    public boolean isExpired(Instant now) {
        return endDate != null && now.isAfter(endDate);
    }

    public boolean isStarted(Instant now) {
        return !now.isBefore(startDate);
    }

    public boolean isUnlimited() {
        return maxUses == 0;
    }

    public boolean hasPerMemberLimit() {
        return perMemberLimit > 0;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        return switch (discountType) {
            case PERCENTAGE -> orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
            case FIXED_AMOUNT -> discountValue.min(orderAmount);
            case FREE_ITEM -> discountValue;
        };
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public String code() { return code; }
    public String name() { return name; }
    public PromoDiscountType discountType() { return discountType; }
    public BigDecimal discountValue() { return discountValue; }
    public BigDecimal minOrderAmount() { return minOrderAmount; }
    public int maxUses() { return maxUses; }
    public int perMemberLimit() { return perMemberLimit; }
    public Instant startDate() { return startDate; }
    public Instant endDate() { return endDate; }
    public boolean isActive() { return active; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
