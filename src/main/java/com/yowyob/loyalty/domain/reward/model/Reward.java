package com.yowyob.loyalty.domain.reward.model;

import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Reward {

    private final UUID id;
    private final TenantId tenantId;
    private String name;
    private String description;
    private final RewardType type;
    private final RewardValue value;
    private final long costInPoints;
    private final Integer stockTotal;
    private Integer stockRemaining;
    private final Instant validFrom;
    private final Instant validUntil;
    private final int grantExpiryDays;
    private String imageUrl;
    private Map<String, Object> metadata;
    private RewardStatus status;
    private int version;
    private final Instant createdAt;
    private Instant updatedAt;

    private Reward(UUID id, TenantId tenantId, String name, String description,
                   RewardType type, RewardValue value, long costInPoints,
                   Integer stockTotal, Integer stockRemaining,
                   Instant validFrom, Instant validUntil, int grantExpiryDays,
                   String imageUrl, Map<String, Object> metadata,
                   RewardStatus status, int version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
        this.costInPoints = costInPoints;
        this.stockTotal = stockTotal;
        this.stockRemaining = stockRemaining;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.grantExpiryDays = grantExpiryDays;
        this.imageUrl = imageUrl;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Reward create(UUID id, TenantId tenantId, String name, String description,
                                RewardType type, RewardValue value, long costInPoints,
                                Integer stockTotal, Instant validFrom, Instant validUntil,
                                int grantExpiryDays, String imageUrl, Map<String, Object> metadata) {
        if (name == null || name.isBlank())
            throw new RewardDomainException("Le nom de la récompense ne peut pas être vide");
        if (costInPoints < 0)
            throw new RewardDomainException("costInPoints doit être >= 0");
        if (stockTotal != null && stockTotal <= 0)
            throw new RewardDomainException("stockTotal doit être > 0 si défini");

        Instant now = Instant.now();
        return new Reward(id, tenantId, name, description, type, value, costInPoints,
                stockTotal, stockTotal, validFrom, validUntil, grantExpiryDays,
                imageUrl, metadata, RewardStatus.DRAFT, 0, now, now);
    }

    public static Reward reconstruct(UUID id, TenantId tenantId, String name, String description,
                                     RewardType type, RewardValue value, long costInPoints,
                                     Integer stockTotal, Integer stockRemaining,
                                     Instant validFrom, Instant validUntil, int grantExpiryDays,
                                     String imageUrl, Map<String, Object> metadata,
                                     RewardStatus status, int version, Instant createdAt, Instant updatedAt) {
        return new Reward(id, tenantId, name, description, type, value, costInPoints,
                stockTotal, stockRemaining, validFrom, validUntil, grantExpiryDays,
                imageUrl, metadata, status, version, createdAt, updatedAt);
    }

    public Reward activate() {
        if (!status.canTransitionTo(RewardStatus.ACTIVE))
            throw new RewardDomainException("Impossible d'activer depuis l'état " + status);
        this.status = RewardStatus.ACTIVE;
        this.updatedAt = Instant.now();
        this.version++;
        return this;
    }

    public Reward pause() {
        if (!status.canTransitionTo(RewardStatus.PAUSED))
            throw new RewardDomainException("Impossible de mettre en pause depuis l'état " + status);
        this.status = RewardStatus.PAUSED;
        this.updatedAt = Instant.now();
        this.version++;
        return this;
    }

    public Reward resume() {
        if (!status.canTransitionTo(RewardStatus.ACTIVE))
            throw new RewardDomainException("Impossible de reprendre depuis l'état " + status);
        this.status = RewardStatus.ACTIVE;
        this.updatedAt = Instant.now();
        this.version++;
        return this;
    }

    public Reward archive() {
        if (!status.canTransitionTo(RewardStatus.ARCHIVED))
            throw new RewardDomainException("Impossible d'archiver depuis l'état " + status);
        this.status = RewardStatus.ARCHIVED;
        this.updatedAt = Instant.now();
        this.version++;
        return this;
    }

    public Reward decrementStock() {
        if (stockRemaining == null) {
            return this;
        }
        if (stockRemaining <= 0) {
            throw new RewardDomainException("Stock épuisé pour la récompense " + name);
        }
        this.stockRemaining--;
        if (this.stockRemaining == 0) {
            this.status = RewardStatus.EXHAUSTED;
        }
        this.updatedAt = Instant.now();
        this.version++;
        return this;
    }

    public Reward restoreStock(int quantity) {
        if (stockRemaining == null) {
            return this;
        }
        this.stockRemaining += quantity;
        if (this.status == RewardStatus.EXHAUSTED) {
            this.status = RewardStatus.ACTIVE;
        }
        this.updatedAt = Instant.now();
        this.version++;
        return this;
    }

    public boolean isAvailableAt(Instant moment) {
        if (!status.isAvailable()) return false;
        if (validFrom != null && moment.isBefore(validFrom)) return false;
        if (validUntil != null && moment.isAfter(validUntil)) return false;
        return true;
    }

    public boolean isRedeemableWithPoints() {
        return costInPoints > 0;
    }

    public Reward update(String name, String description, String imageUrl, Map<String, Object> metadata) {
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (metadata != null) this.metadata = new HashMap<>(metadata);
        this.updatedAt = Instant.now();
        this.version++;
        return this;
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public String name() { return name; }
    public String description() { return description; }
    public RewardType type() { return type; }
    public RewardValue value() { return value; }
    public long costInPoints() { return costInPoints; }
    public Integer stockTotal() { return stockTotal; }
    public Integer stockRemaining() { return stockRemaining; }
    public Instant validFrom() { return validFrom; }
    public Instant validUntil() { return validUntil; }
    public int grantExpiryDays() { return grantExpiryDays; }
    public String imageUrl() { return imageUrl; }
    public Map<String, Object> metadata() { return Map.copyOf(metadata); }
    public RewardStatus status() { return status; }
    public int version() { return version; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
