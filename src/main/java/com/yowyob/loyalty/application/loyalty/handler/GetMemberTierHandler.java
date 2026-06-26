package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.port.in.GetMemberTierUseCase;
import com.yowyob.loyalty.domain.loyalty.port.out.MemberTierRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetMemberTierHandler implements GetMemberTierUseCase {

    private final MemberTierRepository memberTierRepo;

    public GetMemberTierHandler(MemberTierRepository memberTierRepo) {
        this.memberTierRepo = memberTierRepo;
    }

    @Override
    public MemberTier getTier(TenantId tenantId, UserId memberId) {
        return memberTierRepo.findByMemberId(tenantId, memberId)
                .orElseGet(() -> MemberTier.defaultTier(UUID.randomUUID(), tenantId, memberId));
    }
}
