package com.yowyob.loyaulty.program.api.reward;

import com.yowyob.loyaulty.program.api.reward.dto.request.CreateRewardRequest;
import com.yowyob.loyaulty.program.api.reward.dto.response.RewardResponse;
import com.yowyob.loyaulty.program.domain.reward.port.in.RedeemRewardUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin/rewards")
public class RewardAdminController {

    private final RedeemRewardUseCase redeemUseCase;

    public RewardAdminController(RedeemRewardUseCase redeemUseCase) {
        this.redeemUseCase = redeemUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RewardResponse> createReward(@Valid @RequestBody CreateRewardRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> redeemUseCase.createReward(
                        tenantId,
                        request.name(),
                        request.description(),
                        request.type(),
                        request.costPoints(),
                        request.stock(),
                        request.validFrom(),
                        request.validUntil()
                ))
                .map(RewardResponse::from);
    }
}
