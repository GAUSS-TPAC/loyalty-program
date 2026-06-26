package com.yowyob.loyalty.domain.referral.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ReferralLink {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UUID id;
    private final TenantId tenantId;
    private final UserId referrerId;
    private final String code;
    private final Instant createdAt;
    private Instant expiresAt;
    private int usageCount;
    private int conversionCount;
    private boolean active;

    private ReferralLink(UUID id, TenantId tenantId, UserId referrerId, String code,
                          Instant createdAt, Instant expiresAt, int usageCount,
                          int conversionCount, boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.referrerId = referrerId;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usageCount = usageCount;
        this.conversionCount = conversionCount;
        this.active = active;
    }

    public static ReferralLink create(UUID id, TenantId tenantId, UserId referrerId, int expiryDays) {
        Instant now = Instant.now();
        Instant expiresAt = expiryDays > 0 ? now.plus(expiryDays, ChronoUnit.DAYS) : null;
        String code = generateCode();
        return new ReferralLink(id, tenantId, referrerId, code, now, expiresAt, 0, 0, true);
    }

    public static ReferralLink reconstruct(UUID id, TenantId tenantId, UserId referrerId, String code,
                                            Instant createdAt, Instant expiresAt, int usageCount,
                                            int conversionCount, boolean active) {
        return new ReferralLink(id, tenantId, referrerId, code, createdAt, expiresAt, usageCount, conversionCount, active);
    }

    private static String generateCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public void incrementUsage() { this.usageCount++; }
    public void incrementConversion() { this.conversionCount++; }

    public boolean isValid() {
        return active && (expiresAt == null || !Instant.now().isAfter(expiresAt));
    }

    public UUID id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public UserId referrerId() { return referrerId; }
    public String code() { return code; }
    public Instant createdAt() { return createdAt; }
    public Instant expiresAt() { return expiresAt; }
    public int usageCount() { return usageCount; }
    public int conversionCount() { return conversionCount; }
    public boolean isActive() { return active; }
}
