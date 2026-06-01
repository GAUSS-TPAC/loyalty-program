package com.yowyob.loyaulty.program.api.member;

import com.yowyob.loyaulty.program.api.member.dto.response.PointsBalanceResponse;
import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.member.port.in.GetPointsBalanceUseCase;
import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/points")
public class PointsController {

    private final GetPointsBalanceUseCase balanceUseCase;

    public PointsController(GetPointsBalanceUseCase balanceUseCase) {
        this.balanceUseCase = balanceUseCase;
    }

    @GetMapping("/balance")
    public Mono<PointsBalanceResponse> getBalance() {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> balanceUseCase.getBalance(t.getT1(), t.getT2()))
                .map(PointsBalanceResponse::from);
    }

    @GetMapping("/history")
    public Mono<PageResult<PointsTransaction>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> balanceUseCase.getHistory(t.getT1(), t.getT2(),
                        PageRequest.of(page, size)));
    }

    private Mono<String> resolveMemberId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }
}
