package com.yowyob.loyaulty.program.domain.reward.model;

import com.yowyob.loyaulty.program.domain.reward.model.enums.RewardStatus;
import com.yowyob.loyaulty.program.domain.reward.model.enums.RewardType;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.UUID;

public class Reward {

    private final UUID id;
    private final TenantId tenantId;
    private String name;
    private String description;
    private RewardType type;
    private long costPoints;
    private Integer stock;
    private RewardStatus status;
    private Instant validFrom;
    private Instant validUntil;
    private final Instant createdAt;

    private Reward(UUID id, TenantId tenantId, String name, String description,
                   RewardType type, long costPoints, Integer stock,
                   RewardStatus status, Instant validFrom, Instant validUntil,
                   Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.costPoints = costPoints;
        this.stock = stock;
        this.status = status;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
    }

    public static Reward create(TenantId tenantId, String name, String description,
                                 RewardType type, long costPoints, Integer stock,
                                 Instant validFrom, Instant validUntil) {
        if (costPoints < 0) throw new IllegalArgumentException("costPoints must be >= 0");
        return new Reward(UUID.randomUUID(), tenantId, name, description, type,
                costPoints, stock, RewardStatus.ACTIVE, validFrom, validUntil, Instant.now());
    }

    public static Reward reconstitute(UUID id, TenantId tenantId, String name, String description,
                                       RewardType type, long costPoints, Integer stock,
                                       RewardStatus status, Instant validFrom, Instant validUntil,
                                       Instant createdAt) {
        return new Reward(id, tenantId, name, description, type, costPoints,
                stock, status, validFrom, validUntil, createdAt);
    }

    public boolean isAvailable() {
        Instant now = Instant.now();
        if (status != RewardStatus.ACTIVE) return false;
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
        if (stock != null && stock <= 0) return false;
        return true;
    }

    /** Décrémente le stock. Lance une exception si épuisé. */
    public void decrementStock() {
        if (stock == null) return;
        if (stock <= 0) throw new IllegalStateException("Reward out of stock: " + id);
        stock--;
        if (stock == 0) status = RewardStatus.OUT_OF_STOCK;
    }

    public void activate()   { this.status = RewardStatus.ACTIVE; }
    public void deactivate() { this.status = RewardStatus.INACTIVE; }

    public UUID getId()             { return id; }
    public TenantId getTenantId()   { return tenantId; }
    public String getName()         { return name; }
    public String getDescription()  { return description; }
    public RewardType getType()     { return type; }
    public long getCostPoints()     { return costPoints; }
    public Integer getStock()       { return stock; }
    public RewardStatus getStatus() { return status; }
    public Instant getValidFrom()   { return validFrom; }
    public Instant getValidUntil()  { return validUntil; }
    public Instant getCreatedAt()   { return createdAt; }
}
