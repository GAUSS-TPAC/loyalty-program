package com.yowyob.loyalty.api.reward;

import com.yowyob.loyalty.api.reward.dto.request.ConsumeGrantRequestDto;
import com.yowyob.loyalty.api.reward.dto.response.ConsumeGrantResponse;
import com.yowyob.loyalty.api.reward.dto.response.RewardGrantResponse;
import com.yowyob.loyalty.domain.reward.model.ConsumeGrantRequest;
import com.yowyob.loyalty.domain.reward.port.in.ConsumeGrantUseCase;
import com.yowyob.loyalty.domain.reward.port.in.GetMemberGrantsUseCase;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reward-grants")
@Tag(name = "Consume Grants", description = "Consommation des récompenses au checkout")
public class ConsumeGrantController {

    private final ConsumeGrantUseCase consumeGrantUseCase;
    private final GetMemberGrantsUseCase grantsUseCase;

    public ConsumeGrantController(ConsumeGrantUseCase consumeGrantUseCase, GetMemberGrantsUseCase grantsUseCase) {
        this.consumeGrantUseCase = consumeGrantUseCase;
        this.grantsUseCase = grantsUseCase;
    }

    @PostMapping("/{grantId}/consume")
    public Mono<ConsumeGrantResponse> consume(
            @PathVariable UUID grantId,
            @Valid @RequestBody ConsumeGrantRequestDto request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> consumeGrantUseCase.consumeGrant(new ConsumeGrantRequest(
                        tenantId,
                        UserId.of(request.memberId()),
                        grantId,
                        request.orderReference(),
                        request.orderAmount(),
                        idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())))
                .map(ConsumeGrantResponse::from);
    }

    @GetMapping("/{grantId}/validate")
    public Mono<RewardGrantResponse> validate(@PathVariable UUID grantId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> grantsUseCase.getGrant(tenantId, grantId))
                .map(RewardGrantResponse::from);
    }
}
