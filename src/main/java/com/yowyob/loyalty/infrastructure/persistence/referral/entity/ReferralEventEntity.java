package com.yowyob.loyalty.infrastructure.persistence.referral.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("referral_events")
public class ReferralEventEntity {

    @Id
    private UUID id;
    @Column("tenant_id")
    private UUID tenantId;
    @Column("referral_link_id")
    private UUID referralLinkId;
    @Column("referrer_id")
    private UUID referrerId;
    @Column("referee_id")
    private UUID refereeId;
    @Column("enrolled_at")
    private Instant enrolledAt;
    @Column("converted_at")
    private Instant convertedAt;
    private String status;
    @Column("fraud_reason")
    private String fraudReason;
    @Column("conversion_amount")
    private BigDecimal conversionAmount;
    @Column("idempotency_key")
    private String idempotencyKey;
    @Version
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getReferralLinkId() { return referralLinkId; }
    public void setReferralLinkId(UUID referralLinkId) { this.referralLinkId = referralLinkId; }
    public UUID getReferrerId() { return referrerId; }
    public void setReferrerId(UUID referrerId) { this.referrerId = referrerId; }
    public UUID getRefereeId() { return refereeId; }
    public void setRefereeId(UUID refereeId) { this.refereeId = refereeId; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
    public Instant getConvertedAt() { return convertedAt; }
    public void setConvertedAt(Instant convertedAt) { this.convertedAt = convertedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFraudReason() { return fraudReason; }
    public void setFraudReason(String fraudReason) { this.fraudReason = fraudReason; }
    public BigDecimal getConversionAmount() { return conversionAmount; }
    public void setConversionAmount(BigDecimal conversionAmount) { this.conversionAmount = conversionAmount; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
