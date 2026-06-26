package com.yowyob.loyalty.api.reward;

import com.yowyob.loyalty.api.reward.dto.request.RedeemRewardRequest;
import com.yowyob.loyalty.api.reward.dto.response.RedemptionResponse;
import com.yowyob.loyalty.api.reward.dto.response.RewardGrantResponse;
import com.yowyob.loyalty.domain.reward.model.RedemptionRequest;
import com.yowyob.loyalty.domain.reward.port.in.GetMemberGrantsUseCase;
import com.yowyob.loyalty.domain.reward.port.in.RedeemRewardUseCase;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members/me")
@Tag(name = "Member Rewards", description = "Récompenses du membre connecté")
public class RedemptionController {

    private final RedeemRewardUseCase redeemUseCase;
    private final GetMemberGrantsUseCase grantsUseCase;

    public RedemptionController(RedeemRewardUseCase redeemUseCase, GetMemberGrantsUseCase grantsUseCase) {
        this.redeemUseCase = redeemUseCase;
        this.grantsUseCase = grantsUseCase;
    }

    @PostMapping("/redeem")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RedemptionResponse> redeem(
            @Valid @RequestBody RedeemRewardRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMap(tuple -> redeemUseCase.redeem(new RedemptionRequest(
                tuple.getT1(), tuple.getT2(), request.rewardId(),
                idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())))
        .map(RedemptionResponse::from);
    }

    @GetMapping("/grants")
    public Flux<RewardGrantResponse> getActiveGrants() {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMapMany(tuple -> grantsUseCase.getActiveGrants(tuple.getT1(), tuple.getT2()))
        .map(RewardGrantResponse::from);
    }

    @GetMapping("/grants/history")
    public Flux<RewardGrantResponse> getGrantHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMapMany(tuple -> grantsUseCase.getAllGrants(tuple.getT1(), tuple.getT2(), page, size))
        .map(RewardGrantResponse::from);
    }

    @GetMapping("/grants/{grantId}")
    public Mono<RewardGrantResponse> getGrant(@PathVariable UUID grantId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> grantsUseCase.getGrant(tenantId, grantId))
                .map(RewardGrantResponse::from);
    }
}
