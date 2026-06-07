package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.response.MemberTierResponse;
import com.yowyob.loyalty.api.loyalty.dto.response.PointsAccountResponse;
import com.yowyob.loyalty.api.loyalty.dto.response.PointsTransactionResponse;
import com.yowyob.loyalty.domain.loyalty.port.in.GetMemberPointsUseCase;
import com.yowyob.loyalty.domain.loyalty.port.in.GetMemberTierUseCase;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members/{memberId}")
@Tag(name = "Members", description = "Points et tier des membres")
public class MemberLoyaltyController {

    private final GetMemberPointsUseCase getMemberPointsUseCase;
    private final GetMemberTierUseCase getMemberTierUseCase;

    public MemberLoyaltyController(
            GetMemberPointsUseCase getMemberPointsUseCase,
            GetMemberTierUseCase getMemberTierUseCase
    ) {
        this.getMemberPointsUseCase = getMemberPointsUseCase;
        this.getMemberTierUseCase = getMemberTierUseCase;
    }

    @GetMapping("/points")
    public Mono<PointsAccountResponse> getPoints(@PathVariable UUID memberId) {
        return TenantContextHolder.getTenantId()
                .map(tenantId -> {
                    UserId userId = UserId.of(memberId);
                    var account = getMemberPointsUseCase.getPoints(tenantId, userId);
                    var tier = getMemberTierUseCase.getTier(tenantId, userId);
                    return PointsAccountResponse.from(account, tier);
                });
    }

    @GetMapping("/points/history")
    public Flux<PointsTransactionResponse> getPointsHistory(
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> Flux.fromIterable(
                        getMemberPointsUseCase.getPointsHistory(tenantId, UserId.of(memberId), page, size)))
                .map(PointsTransactionResponse::from);
    }

    @GetMapping("/tier")
    public Mono<MemberTierResponse> getTier(@PathVariable UUID memberId) {
        return TenantContextHolder.getTenantId()
                .map(tenantId -> MemberTierResponse.from(
                        getMemberTierUseCase.getTier(tenantId, UserId.of(memberId))));
    }
}
