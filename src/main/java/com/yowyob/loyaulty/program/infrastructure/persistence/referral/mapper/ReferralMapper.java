package com.yowyob.loyaulty.program.infrastructure.persistence.referral.mapper;

import com.yowyob.loyaulty.program.domain.referral.model.*;
import com.yowyob.loyaulty.program.domain.referral.model.enums.ReferralStatus;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.referral.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ReferralMapper {

    // ── ReferralProgram ───────────────────────────────────────────────────

    public ReferralProgram toDomain(ReferralProgramEntity e) {
        return new ReferralProgram(
                e.getId(), TenantId.of(e.getTenantId()),
                e.isActive(),
                e.getReferrerRewardType(), e.getReferrerRewardValue(),
                e.getRefereeRewardType(),  e.getRefereeRewardValue(),
                e.getConversionEventType(), e.getMinConversionAmount(),
                e.getConversionDeadlineDays()
        );
    }

    public ReferralProgramEntity toEntity(ReferralProgram p) {
        ReferralProgramEntity e = new ReferralProgramEntity();
        e.setId(p.id());
        e.setTenantId(p.tenantId().value());
        e.setActive(p.active());
        e.setReferrerRewardType(p.referrerRewardType());
        e.setReferrerRewardValue(p.referrerRewardValue());
        e.setRefereeRewardType(p.refereeRewardType());
        e.setRefereeRewardValue(p.refereeRewardValue());
        e.setConversionEventType(p.conversionEventType());
        e.setMinConversionAmount(p.minConversionAmount());
        e.setConversionDeadlineDays(p.conversionDeadlineDays());
        return e;
    }

    // ── ReferralLink ──────────────────────────────────────────────────────

    public ReferralLink toDomain(ReferralLinkEntity e) {
        return new ReferralLink(e.getId(), TenantId.of(e.getTenantId()),
                e.getReferrerId(), e.getCode(), e.getCreatedAt());
    }

    public ReferralLinkEntity toEntity(ReferralLink l) {
        ReferralLinkEntity e = new ReferralLinkEntity();
        e.setId(l.id());
        e.setTenantId(l.tenantId().value());
        e.setReferrerId(l.referrerId());
        e.setCode(l.code());
        e.setCreatedAt(l.createdAt());
        return e;
    }

    // ── ReferralEvent ─────────────────────────────────────────────────────

    public ReferralEvent toDomain(ReferralEventEntity e) {
        return ReferralEvent.reconstitute(
                e.getId(), TenantId.of(e.getTenantId()),
                e.getReferrerId(), e.getRefereeId(), e.getReferralCode(),
                ReferralStatus.valueOf(e.getStatus()),
                e.getEnrolledAt(), e.getConvertedAt(), e.getRewardedAt(), e.getExpiresAt()
        );
    }

    public ReferralEventEntity toEntity(ReferralEvent r) {
        ReferralEventEntity e = new ReferralEventEntity();
        e.setId(r.getId());
        e.setTenantId(r.getTenantId().value());
        e.setReferrerId(r.getReferrerId());
        e.setRefereeId(r.getRefereeId());
        e.setReferralCode(r.getReferralCode());
        e.setStatus(r.getStatus().name());
        e.setEnrolledAt(r.getEnrolledAt());
        e.setConvertedAt(r.getConvertedAt());
        e.setRewardedAt(r.getRewardedAt());
        e.setExpiresAt(r.getExpiresAt());
        return e;
    }
}
