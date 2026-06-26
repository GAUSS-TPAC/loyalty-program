package com.yowyob.loyalty.domain.referral.model;

import com.yowyob.loyalty.domain.referral.exception.ReferralAlreadyConvertedException;
import com.yowyob.loyalty.domain.referral.exception.ReferralDomainException;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ReferralEvent {

    private final UUID id;
    private final TenantId tenantId;
    private final UUID referralLinkId;
    private final UserId referrerId;
    private final UserId refereeId;
    private final Instant enrolledAt;
    private Instant convertedAt;
    private ReferralStatus status;
    private String fraudReason;
    private BigDecimal conversionAmount;

    private ReferralEvent(UUID id, TenantId tenantId, UUID referralLinkId, UserId referrerId,
                           UserId refereeId, Instant enrolledAt, Instant convertedAt,
                           ReferralStatus status, String fraudReason, BigDecimal conversionAmount) {
        this.id = id;
        this.tenantId = tenantId;
        this.referralLinkId = referralLinkId;
        this.referrerId = referrerId;
        this.refereeId = refereeId;
        this.enrolledAt = enrolledAt;
        this.convertedAt = convertedAt;
        this.status = status;
        this.fraudReason = fraudReason;
        this.conversionAmount = conversionAmount;
    }

    public static ReferralEvent create(UUID id, TenantId tenantId, UUID linkId,
                                        UserId referrerId, UserId refereeId) {
        return new ReferralEvent(id, tenantId, linkId, referrerId, refereeId,
                Instant.now(), null, ReferralStatus.PENDING, null, null);
    }

    public static ReferralEvent reconstruct(UUID id, TenantId tenantId, UUID referralLinkId,
                                             UserId referrerId, UserId refereeId, Instant enrolledAt,
                                             Instant convertedAt, ReferralStatus status,
                                             String fraudReason, BigDecimal conversionAmount) {
        return new ReferralEvent(id, tenantId, referralLinkId, referrerId, refereeId,
                enrolledAt, convertedAt, status, fraudReason, conversionAmount);
    }

    public ReferralEvent enroll() {
        if (this.status != ReferralStatus.PENDING)
            throw new ReferralDomainException("L'événement n'est pas en état PENDING");
        this.status = ReferralStatus.ENROLLED;
        return this;
    }

    public ReferralEvent convert(BigDecimal amount) {
        if (status.isFinal())
            throw new ReferralAlreadyConvertedException(id);
        this.status = ReferralStatus.CONVERTED;
        this.convertedAt = Instant.now();
        this.conversionAmount = amount;
        return this;
    }

    public ReferralEvent markFraud(String reason) {
        this.status = ReferralStatus.FRAUD;
        this.fraudReason = reason;
        return this;
    }

    public ReferralEvent expire() {
        this.status = ReferralStatus.EXPIRED;
        return this;
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public UUID referralLinkId() { return referralLinkId; }
    public UserId referrerId() { return referrerId; }
    public UserId refereeId() { return refereeId; }
    public Instant enrolledAt() { return enrolledAt; }
    public Instant convertedAt() { return convertedAt; }
    public ReferralStatus status() { return status; }
    public String fraudReason() { return fraudReason; }
    public BigDecimal conversionAmount() { return conversionAmount; }
}
