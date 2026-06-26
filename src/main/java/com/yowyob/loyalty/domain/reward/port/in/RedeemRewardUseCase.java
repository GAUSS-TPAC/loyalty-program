package com.yowyob.loyalty.domain.reward.port.in;

import com.yowyob.loyalty.domain.reward.model.RedemptionRequest;
import com.yowyob.loyalty.domain.reward.model.RedemptionResult;
import reactor.core.publisher.Mono;

public interface RedeemRewardUseCase {
    Mono<RedemptionResult> redeem(RedemptionRequest request);
}
