package com.yowyob.loyalty.infrastructure.persistence.referral.mapper;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.referral.model.ReferralStatus;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralEventEntity;
import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralLinkEntity;
import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralProgramEntity;
import org.springframework.stereotype.Component;

@Component
public class ReferralMapper {

    public ReferralProgram toDomain(ReferralProgramEntity e) {
        return ReferralProgram.reconstruct(
                e.getId(), TenantId.of(e.getTenantId()), e.getName(), e.isActive(),
                e.getMaxReferralsPerReferrer(), e.getReferralWindowDays(),
                e.getReferrerRewardId(), e.getRefereeRewardId(),
                e.getMinConversionAmount(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public ReferralProgramEntity toEntity(ReferralProgram p) {
        ReferralProgramEntity e = new ReferralProgramEntity();
        e.setId(p.id());
        e.setTenantId(p.tenantId().value());
        e.setName(p.name());
        e.setActive(p.isActive());
        e.setMaxReferralsPerReferrer(p.maxReferralsPerReferrer());
        e.setReferralWindowDays(p.referralWindowDays());
        e.setReferrerRewardId(p.referrerRewardId());
        e.setRefereeRewardId(p.refereeRewardId());
        e.setMinConversionAmount(p.minConversionAmount());
        e.setCreatedAt(p.createdAt());
        e.setUpdatedAt(p.updatedAt());
        return e;
    }

    public ReferralLink toDomain(ReferralLinkEntity e) {
        return ReferralLink.reconstruct(
                e.getId(), TenantId.of(e.getTenantId()),
                UserId.of(e.getReferrerId()), e.getCode(),
                e.getCreatedAt(), e.getExpiresAt(),
                e.getUsageCount(), e.getConversionCount(), e.isActive());
    }

    public ReferralLinkEntity toEntity(ReferralLink l) {
        ReferralLinkEntity e = new ReferralLinkEntity();
        e.setId(l.id());
        e.setTenantId(l.tenantId().value());
        e.setReferrerId(l.referrerId().value());
        e.setCode(l.code());
        e.setCreatedAt(l.createdAt());
        e.setExpiresAt(l.expiresAt());
        e.setUsageCount(l.usageCount());
        e.setConversionCount(l.conversionCount());
        e.setActive(l.isActive());
        return e;
    }

    public ReferralEvent toDomain(ReferralEventEntity e) {
        return ReferralEvent.reconstruct(
                e.getId(), TenantId.of(e.getTenantId()), e.getReferralLinkId(),
                UserId.of(e.getReferrerId()), UserId.of(e.getRefereeId()),
                e.getEnrolledAt(), e.getConvertedAt(),
                ReferralStatus.valueOf(e.getStatus()),
                e.getFraudReason(), e.getConversionAmount());
    }

    public ReferralEventEntity toEntity(ReferralEvent ev) {
        ReferralEventEntity e = new ReferralEventEntity();
        e.setId(ev.id());
        e.setTenantId(ev.tenantId().value());
        e.setReferralLinkId(ev.referralLinkId());
        e.setReferrerId(ev.referrerId().value());
        e.setRefereeId(ev.refereeId().value());
        e.setEnrolledAt(ev.enrolledAt());
        e.setConvertedAt(ev.convertedAt());
        e.setStatus(ev.status().name());
        e.setFraudReason(ev.fraudReason());
        e.setConversionAmount(ev.conversionAmount());
        return e;
    }
}
