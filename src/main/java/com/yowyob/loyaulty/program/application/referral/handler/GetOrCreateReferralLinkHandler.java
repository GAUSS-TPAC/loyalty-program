package com.yowyob.loyaulty.program.application.referral.handler;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralLink;
import com.yowyob.loyaulty.program.domain.referral.port.in.GetReferralLinkUseCase;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralLinkRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetOrCreateReferralLinkHandler implements GetReferralLinkUseCase {

    private final ReferralLinkRepository linkRepository;

    public GetOrCreateReferralLinkHandler(ReferralLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public Mono<ReferralLink> getOrCreate(TenantId tenantId, String memberId) {
        return linkRepository.findByReferrerId(tenantId, memberId)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            ReferralLink newLink = ReferralLink.create(tenantId, memberId);
                            return linkRepository.save(newLink);
                        })
                );
    }
}
