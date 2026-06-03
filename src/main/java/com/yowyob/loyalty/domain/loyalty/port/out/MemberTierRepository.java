package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.util.Optional;

public interface MemberTierRepository {
    MemberTier save(MemberTier memberTier);
    Optional<MemberTier> findByMemberId(TenantId tenantId, UserId memberId);
}
