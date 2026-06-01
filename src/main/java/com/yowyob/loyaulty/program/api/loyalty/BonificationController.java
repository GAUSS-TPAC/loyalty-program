package com.yowyob.loyaulty.program.api.loyalty;

import com.yowyob.loyaulty.program.api.loyalty.dto.request.BonificationTransactionRequest;
import com.yowyob.loyaulty.program.api.loyalty.dto.response.BonificationRewardResponse;
import com.yowyob.loyaulty.program.api.loyalty.dto.response.PointsResultResponse;
import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationReward;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.domain.loyalty.port.in.ProcessBonificationUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/loyalty")
public class BonificationController {

    private final ProcessBonificationUseCase useCase;

    public BonificationController(ProcessBonificationUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PointsResultResponse> recordTransaction(
            @Valid @RequestBody BonificationTransactionRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(tuple -> useCase.processTransaction(
                        tuple.getT1(),
                        tuple.getT2(),
                        request.amount(),
                        request.description(),
                        idempotencyKey
                ))
                .map(this::toResponse);
    }

    @GetMapping("/transactions")
    public Flux<PointsResultResponse> getTransactionHistory() {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMapMany(tuple -> useCase.getMemberTransactionHistory(
                        tuple.getT1(),
                        tuple.getT2()
                ))
                .map(tx -> new PointsResultResponse(
                        tx.transactionId(),
                        tx.pointsEarned(),
                        null,
                        false,
                        null
                ));
    }

    private Mono<String> resolveMemberId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }

    private PointsResultResponse toResponse(PointsResult result) {
        BonificationRewardResponse rewardResponse = result.hasReward()
                ? toRewardResponse(result.triggeredReward())
                : null;
        return new PointsResultResponse(
                result.transactionId(),
                result.pointsEarned(),
                result.totalPoints(),
                result.rewardTriggered(),
                rewardResponse
        );
    }

    private BonificationRewardResponse toRewardResponse(BonificationReward reward) {
        return new BonificationRewardResponse(
                reward.rewardId(),
                reward.name(),
                reward.description(),
                reward.type(),
                reward.value()
        );
    }
}
