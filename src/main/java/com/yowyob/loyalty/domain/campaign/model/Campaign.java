package com.yowyob.loyalty.domain.campaign.model;

import com.yowyob.loyalty.domain.campaign.exception.CampaignDomainException;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Campaign {

    private final UUID id;
    private final TenantId tenantId;
    private String name;
    private String description;
    private final CampaignType campaignType;
    private final String targetEventType;
    private final BigDecimal bonusMultiplier;
    private final long bonusPoints;
    private final Instant startDate;
    private final Instant endDate;
    private CampaignStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Campaign(UUID id, TenantId tenantId, String name, String description,
                     CampaignType campaignType, String targetEventType,
                     BigDecimal bonusMultiplier, long bonusPoints,
                     Instant startDate, Instant endDate, CampaignStatus status,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.campaignType = campaignType;
        this.targetEventType = targetEventType;
        this.bonusMultiplier = bonusMultiplier;
        this.bonusPoints = bonusPoints;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Campaign create(UUID id, TenantId tenantId, String name, String description,
                                   CampaignType campaignType, String targetEventType,
                                   BigDecimal bonusMultiplier, long bonusPoints,
                                   Instant startDate, Instant endDate) {
        if (name == null || name.isBlank())
            throw new CampaignDomainException("Le nom de la campagne est obligatoire");
        if (startDate == null)
            throw new CampaignDomainException("La date de début est obligatoire");
        if (endDate != null && endDate.isBefore(startDate))
            throw new CampaignDomainException("La date de fin doit être après la date de début");
        if (campaignType == CampaignType.BONUS_MULTIPLIER && (bonusMultiplier == null || bonusMultiplier.compareTo(BigDecimal.ONE) <= 0))
            throw new CampaignDomainException("Le multiplicateur doit être supérieur à 1.0");
        if (campaignType == CampaignType.FLAT_BONUS && bonusPoints <= 0)
            throw new CampaignDomainException("Le bonus de points doit être positif");

        Instant now = Instant.now();
        return new Campaign(id, tenantId, name, description, campaignType, targetEventType,
                bonusMultiplier != null ? bonusMultiplier : BigDecimal.ONE,
                bonusPoints, startDate, endDate, CampaignStatus.DRAFT, now, now);
    }

    public static Campaign reconstruct(UUID id, TenantId tenantId, String name, String description,
                                        CampaignType campaignType, String targetEventType,
                                        BigDecimal bonusMultiplier, long bonusPoints,
                                        Instant startDate, Instant endDate, CampaignStatus status,
                                        Instant createdAt, Instant updatedAt) {
        return new Campaign(id, tenantId, name, description, campaignType, targetEventType,
                bonusMultiplier, bonusPoints, startDate, endDate, status, createdAt, updatedAt);
    }

    public Campaign activate() {
        if (status.isTerminal())
            throw new CampaignDomainException("Impossible d'activer une campagne terminée");
        this.status = CampaignStatus.ACTIVE;
        this.updatedAt = Instant.now();
        return this;
    }

    public Campaign pause() {
        if (status != CampaignStatus.ACTIVE)
            throw new CampaignDomainException("Seule une campagne active peut être mise en pause");
        this.status = CampaignStatus.PAUSED;
        this.updatedAt = Instant.now();
        return this;
    }

    public Campaign complete() {
        this.status = CampaignStatus.COMPLETED;
        this.updatedAt = Instant.now();
        return this;
    }

    public Campaign cancel() {
        if (status.isTerminal())
            throw new CampaignDomainException("La campagne est déjà terminée");
        this.status = CampaignStatus.CANCELLED;
        this.updatedAt = Instant.now();
        return this;
    }

    public boolean isDueForActivation(Instant now) {
        return status == CampaignStatus.DRAFT
                && !now.isBefore(startDate)
                && (endDate == null || now.isBefore(endDate));
    }

    public boolean isDueForCompletion(Instant now) {
        return status == CampaignStatus.ACTIVE
                && endDate != null
                && !now.isBefore(endDate);
    }

    public boolean appliesToEvent(String eventType) {
        return targetEventType == null || targetEventType.isBlank() || targetEventType.equals(eventType);
    }

    public long calculateExtraPoints(long pointsEarned) {
        return switch (campaignType) {
            case BONUS_MULTIPLIER -> (long) (pointsEarned * (bonusMultiplier.doubleValue() - 1));
            case FLAT_BONUS -> bonusPoints;
        };
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public String name() { return name; }
    public String description() { return description; }
    public CampaignType campaignType() { return campaignType; }
    public String targetEventType() { return targetEventType; }
    public BigDecimal bonusMultiplier() { return bonusMultiplier; }
    public long bonusPoints() { return bonusPoints; }
    public Instant startDate() { return startDate; }
    public Instant endDate() { return endDate; }
    public CampaignStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
