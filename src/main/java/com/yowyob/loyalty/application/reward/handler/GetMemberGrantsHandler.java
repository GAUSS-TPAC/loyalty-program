package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.in.GetMemberGrantsUseCase;
import com.yowyob.loyalty.domain.reward.port.out.RewardGrantRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetMemberGrantsHandler implements GetMemberGrantsUseCase {

    private final RewardGrantRepository grantRepository;

    public GetMemberGrantsHandler(RewardGrantRepository grantRepository) {
        this.grantRepository = grantRepository;
    }

    @Override
    public Flux<RewardGrant> getActiveGrants(TenantId tenantId, UserId memberId) {
        return grantRepository.findActiveByMember(memberId, tenantId);
    }

    @Override
    public Flux<RewardGrant> getAllGrants(TenantId tenantId, UserId memberId, int page, int size) {
        return grantRepository.findAllByMember(memberId, tenantId, page, size);
    }

    @Override
    public Mono<RewardGrant> getGrant(TenantId tenantId, UUID grantId) {
        return grantRepository.findByIdAndTenant(grantId, tenantId);
    }
}
