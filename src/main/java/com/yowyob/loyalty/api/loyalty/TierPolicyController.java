package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.request.TierPolicyRequest;
import com.yowyob.loyalty.api.loyalty.dto.response.TierPolicyResponse;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.loyalty.port.in.UpdateTierPolicyUseCase;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tier-policies")
@Tag(name = "Tier Policy", description = "Configuration des niveaux de fidélité")
public class TierPolicyController {

    private final UpdateTierPolicyUseCase tierPolicyUseCase;

    public TierPolicyController(UpdateTierPolicyUseCase tierPolicyUseCase) {
        this.tierPolicyUseCase = tierPolicyUseCase;
    }

    @GetMapping
    public Mono<TierPolicyResponse> getTierPolicy() {
        return TenantContextHolder.getTenantId()
                .flatMap(tierPolicyUseCase::getTierPolicy)
                .map(TierPolicyResponse::from);
    }

    @PutMapping
    public Mono<TierPolicyResponse> upsertTierPolicy(@Valid @RequestBody TierPolicyRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> {
                    List<TierPolicy.TierThreshold> thresholds = request.thresholds().stream()
                            .map(t -> new TierPolicy.TierThreshold(
                                    TierLevel.valueOf(t.level()),
                                    t.threshold(),
                                    t.multiplier()))
                            .toList();
                    TierPolicy policy = new TierPolicy(
                            tenantId,
                            request.criterion(),
                            thresholds,
                            request.maintainPeriod(),
                            request.maintainThresholdPoints(),
                            request.downgradeGraceDays());
                    return tierPolicyUseCase.upsertTierPolicy(policy);
                })
                .map(TierPolicyResponse::from);
    }
}
