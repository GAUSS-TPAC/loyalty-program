package com.yowyob.loyaulty.program.api.reward;

import com.yowyob.loyaulty.program.api.reward.dto.response.RewardGrantResponse;
import com.yowyob.loyaulty.program.api.reward.dto.response.RewardResponse;
import com.yowyob.loyaulty.program.domain.reward.port.in.RedeemRewardUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final RedeemRewardUseCase redeemUseCase;

    public RewardController(RedeemRewardUseCase redeemUseCase) {
        this.redeemUseCase = redeemUseCase;
    }

    /** Catalogue des récompenses disponibles pour le tenant. */
    @GetMapping
    public Flux<RewardResponse> listCatalog() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(redeemUseCase::listCatalog)
                .map(RewardResponse::from);
    }

    /** Échange des points contre une récompense. */
    @PostMapping("/{rewardId}/redeem")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RewardGrantResponse> redeem(@PathVariable UUID rewardId) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> redeemUseCase.redeem(t.getT1(), t.getT2(), rewardId))
                .map(RewardGrantResponse::from);
    }

    /** Liste les récompenses actives du membre courant. */
    @GetMapping("/grants")
    public Flux<RewardGrantResponse> listMyGrants() {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMapMany(t -> redeemUseCase.listActiveGrants(t.getT1(), t.getT2()))
                .map(RewardGrantResponse::from);
    }

    /** Marque un grant comme utilisé (validation par la plateforme). */
    @PostMapping("/grants/{grantId}/consume")
    public Mono<RewardGrantResponse> consume(@PathVariable UUID grantId,
                                              @RequestParam(required = false) String context) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> redeemUseCase.consume(tenantId, grantId,
                        context != null ? context : "{}"))
                .map(RewardGrantResponse::from);
    }

    private Mono<String> resolveMemberId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }
}
