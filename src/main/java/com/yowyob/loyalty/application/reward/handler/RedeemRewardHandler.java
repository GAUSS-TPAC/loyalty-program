package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.model.RedemptionRequest;
import com.yowyob.loyalty.domain.reward.model.RedemptionResult;
import com.yowyob.loyalty.domain.reward.port.in.RedeemRewardUseCase;
import com.yowyob.loyalty.domain.reward.service.RedemptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class RedeemRewardHandler implements RedeemRewardUseCase {

    private final RedemptionService redemptionService;

    public RedeemRewardHandler(RedemptionService redemptionService) {
        this.redemptionService = redemptionService;
    }

    @Override
    @Transactional
    public Mono<RedemptionResult> redeem(RedemptionRequest request) {
        return redemptionService.redeem(request);
    }
}
